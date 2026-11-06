"""Extract an OpenType MATH font into the compact binary read by MathFont.kt.

Run from the repository root:

    uv run --with fonttools python mallet-math/scripts/extract_font.py \
        path/to/latinmodern-math.otf \
        mallet-math/src/main/resources/com/jocmp/malletmath/math-glyphs.bin

The output keeps only what the typesetter needs: font units per em, the MATH
constants, the cmap, per-glyph metrics and outlines, and the MATH variant and
assembly tables. Glyphs not reachable from the cmap or the MATH tables are
dropped. All integers are big-endian to match java.io.DataInputStream.
"""

import struct
import sys

from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.recordingPen import RecordingPen
from fontTools.ttLib import TTFont

MAGIC = b"MMF1"
NO_ACCENT = 0x7FFF

# Order matches MathConstant in MathFont.kt.
CONSTANTS = [
    "ScriptPercentScaleDown",
    "ScriptScriptPercentScaleDown",
    "DelimitedSubFormulaMinHeight",
    "DisplayOperatorMinHeight",
    "MathLeading",
    "AxisHeight",
    "AccentBaseHeight",
    "FlattenedAccentBaseHeight",
    "SubscriptShiftDown",
    "SubscriptTopMax",
    "SubscriptBaselineDropMin",
    "SuperscriptShiftUp",
    "SuperscriptShiftUpCramped",
    "SuperscriptBottomMin",
    "SuperscriptBaselineDropMax",
    "SubSuperscriptGapMin",
    "SuperscriptBottomMaxWithSubscript",
    "SpaceAfterScript",
    "UpperLimitGapMin",
    "UpperLimitBaselineRiseMin",
    "LowerLimitGapMin",
    "LowerLimitBaselineDropMin",
    "StackTopShiftUp",
    "StackTopDisplayStyleShiftUp",
    "StackBottomShiftDown",
    "StackBottomDisplayStyleShiftDown",
    "StackGapMin",
    "StackDisplayStyleGapMin",
    "StretchStackTopShiftUp",
    "StretchStackBottomShiftDown",
    "StretchStackGapAboveMin",
    "StretchStackGapBelowMin",
    "FractionNumeratorShiftUp",
    "FractionNumeratorDisplayStyleShiftUp",
    "FractionDenominatorShiftDown",
    "FractionDenominatorDisplayStyleShiftDown",
    "FractionNumeratorGapMin",
    "FractionNumDisplayStyleGapMin",
    "FractionRuleThickness",
    "FractionDenominatorGapMin",
    "FractionDenomDisplayStyleGapMin",
    "SkewedFractionHorizontalGap",
    "SkewedFractionVerticalGap",
    "OverbarVerticalGap",
    "OverbarRuleThickness",
    "OverbarExtraAscender",
    "UnderbarVerticalGap",
    "UnderbarRuleThickness",
    "UnderbarExtraDescender",
    "RadicalVerticalGap",
    "RadicalDisplayStyleVerticalGap",
    "RadicalRuleThickness",
    "RadicalExtraAscender",
    "RadicalKernBeforeDegree",
    "RadicalKernAfterDegree",
    "RadicalDegreeBottomRaisePercent",
]

OP_MOVE = 0
OP_LINE = 1
OP_QUAD = 2
OP_CUBIC = 3
OP_CLOSE = 4


def main(source, destination):
    font = TTFont(source)
    order = font.getGlyphOrder()
    gid_of = {name: gid for gid, name in enumerate(order)}
    glyph_set = font.getGlyphSet()
    math = font["MATH"].table

    cmap = sorted(font.getBestCmap().items())
    vertical = constructions(math.MathVariants.VertGlyphCoverage, math.MathVariants.VertGlyphConstruction, gid_of)
    horizontal = constructions(math.MathVariants.HorizGlyphCoverage, math.MathVariants.HorizGlyphConstruction, gid_of)

    keep = {0} | {gid_of[name] for _, name in cmap}
    for table in (vertical, horizontal):
        for gid, (variants, parts) in table.items():
            keep.add(gid)
            keep.update(variants)
            keep.update(part[0] for part in parts)

    info = math.MathGlyphInfo
    italics = matched(info.MathItalicsCorrectionInfo.Coverage, info.MathItalicsCorrectionInfo.ItalicsCorrection, gid_of)
    accents = matched(info.MathTopAccentAttachment.TopAccentCoverage, info.MathTopAccentAttachment.TopAccentAttachment, gid_of)

    out = bytearray()
    out += MAGIC
    out += struct.pack(">H", font["head"].unitsPerEm)

    out += struct.pack(">H", len(CONSTANTS))
    for name in CONSTANTS:
        value = getattr(math.MathConstants, name)
        out += struct.pack(">h", getattr(value, "Value", value))
    out += struct.pack(">H", math.MathVariants.MinConnectorOverlap)

    out += struct.pack(">I", len(cmap))
    for codepoint, name in cmap:
        out += struct.pack(">IH", codepoint, gid_of[name])

    glyphs = sorted(keep)
    out += struct.pack(">H", len(glyphs))
    for gid in glyphs:
        name = order[gid]
        glyph = glyph_set[name]
        bounds = BoundsPen(glyph_set)
        glyph.draw(bounds)
        x_min, y_min, x_max, y_max = (round(v) for v in (bounds.bounds or (0, 0, 0, 0)))
        out += struct.pack(
            ">Hhhhhhhh",
            gid,
            round(glyph.width),
            x_min,
            y_min,
            x_max,
            y_max,
            italics.get(gid, 0),
            accents.get(gid, NO_ACCENT),
        )
        out += outline(glyph, glyph_set)

    write_constructions(out, vertical, with_assembly=True)
    write_constructions(out, horizontal, with_assembly=False)

    with open(destination, "wb") as handle:
        handle.write(out)

    print(f"{len(glyphs)} glyphs, {len(cmap)} codepoints, {len(out)} bytes -> {destination}")


def constructions(coverage, records, gid_of):
    result = {}
    for name, record in zip(coverage.glyphs, records):
        variants = [gid_of[v.VariantGlyph] for v in record.MathGlyphVariantRecord]
        parts = []
        if record.GlyphAssembly is not None:
            parts = [
                (
                    gid_of[p.glyph],
                    p.StartConnectorLength,
                    p.EndConnectorLength,
                    p.FullAdvance,
                    p.PartFlags,
                )
                for p in record.GlyphAssembly.PartRecords
            ]
        result[gid_of[name]] = (variants, parts)
    return result


def matched(coverage, values, gid_of):
    return {gid_of[name]: value.Value for name, value in zip(coverage.glyphs, values)}


def outline(glyph, glyph_set):
    pen = RecordingPen()
    glyph.draw(pen)

    ops = bytearray()
    count = 0
    for op, points in pen.value:
        if op == "moveTo":
            ops += struct.pack(">B", OP_MOVE) + coords(points)
            count += 1
        elif op == "lineTo":
            ops += struct.pack(">B", OP_LINE) + coords(points)
            count += 1
        elif op == "curveTo":
            ops += struct.pack(">B", OP_CUBIC) + coords(points)
            count += 1
        elif op == "qCurveTo":
            # TrueType implied on-curve points are split into explicit quads.
            *controls, end = points
            for index, control in enumerate(controls):
                if index + 1 < len(controls):
                    following = controls[index + 1]
                    target = ((control[0] + following[0]) / 2, (control[1] + following[1]) / 2)
                else:
                    target = end
                ops += struct.pack(">B", OP_QUAD) + coords([control, target])
                count += 1
        elif op in ("closePath", "endPath"):
            ops += struct.pack(">B", OP_CLOSE)
            count += 1
        else:
            raise ValueError(f"unsupported pen op {op}")

    return struct.pack(">H", count) + ops


def coords(points):
    return b"".join(struct.pack(">hh", round(x), round(y)) for x, y in points)


def write_constructions(out, table, with_assembly):
    out += struct.pack(">H", len(table))
    for gid in sorted(table):
        variants, parts = table[gid]
        out += struct.pack(">HH", gid, len(variants))
        for variant in variants:
            out += struct.pack(">H", variant)
        if with_assembly:
            out += struct.pack(">H", len(parts))
            for glyph, start, end, full, flags in parts:
                out += struct.pack(">HhhhH", glyph, start, end, full, flags)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
