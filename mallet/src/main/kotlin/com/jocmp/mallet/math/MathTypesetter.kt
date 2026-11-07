package com.jocmp.mallet.math

/**
 * Lays out a [MathList] following TeX's Appendix G rules with OpenType MATH metrics.
 *
 * Derived from AndroidMath's MTTypesetter (MIT), itself a port of iosMath.
 */
internal class MathTypesetter private constructor(
    private val font: MathMetrics,
    style: LineStyle,
    private val cramped: Boolean,
    private val spaced: Boolean,
) {
    private val children = mutableListOf<MathNode>()
    private var x = 0f
    private var style: LineStyle = style
    private var styleFont: MathMetrics = font.withSize(styleSize(style))

    companion object {
        fun layout(list: MathList, font: MathMetrics, style: LineStyle): MathBox =
            createLine(list, font, style, cramped = false)

        private fun createLine(
            list: MathList,
            font: MathMetrics,
            style: LineStyle,
            cramped: Boolean,
            spaced: Boolean = false,
        ): MathBox {
            val typesetter = MathTypesetter(font, style, cramped, spaced)
            return typesetter.typeset(preprocess(list))
        }

        /**
         * Applies TeX rules 5, 6 and 14: binary operators become unary where they can't be
         * binary, adjacent digits fuse into one number, and adjacent ordinaries fuse into one
         * run. Variables and numbers take their final glyphs here. Atoms are copied, not mutated,
         * so a list can be laid out more than once.
         */
        private fun preprocess(list: MathList): List<Item> {
            val finalized = mutableListOf<Item>()

            list.forEach { atom ->
                val item = Item(atom)
                // For operator rules, style changes and explicit spaces don't count as neighbors, so
                // a cell that opens with \textstyle still treats a leading minus as unary.
                val prev = finalized.lastOrNull { it.type != AtomType.STYLE && it.type != AtomType.SPACE }
                // Digits only fuse when directly adjacent, so 10\,000 keeps its thin space.
                val adjacent = finalized.lastOrNull()

                when (item.type) {
                    AtomType.BINARY_OPERATOR ->
                        if (isNotBinaryOperator(prev)) {
                            item.type = AtomType.UNARY_OPERATOR
                        }

                    AtomType.RELATION, AtomType.PUNCTUATION, AtomType.CLOSE ->
                        if (prev?.type == AtomType.BINARY_OPERATOR) {
                            prev.type = AtomType.UNARY_OPERATOR
                        }

                    AtomType.NUMBER ->
                        if (adjacent != null && adjacent.type == AtomType.NUMBER && adjacent.isPlain && !adjacent.hasScripts) {
                            adjacent.fuse(item)
                            return@forEach
                        }

                    else -> Unit
                }
                finalized.add(item)
            }

            finalized.lastOrNull { it.type != AtomType.STYLE && it.type != AtomType.SPACE }?.let { last ->
                if (last.type == AtomType.BINARY_OPERATOR) {
                    last.type = AtomType.UNARY_OPERATOR
                }
            }

            val preprocessed = mutableListOf<Item>()
            finalized.forEach { item ->
                if (item.isPlain && (item.type == AtomType.VARIABLE || item.type == AtomType.NUMBER)) {
                    item.nucleus = MathCharset.styled(item.nucleus, item.atom.fontStyle)
                    item.type = AtomType.ORDINARY
                } else if (item.type == AtomType.UNARY_OPERATOR) {
                    item.type = AtomType.ORDINARY
                }

                val prev = preprocessed.lastOrNull()
                if (item.type == AtomType.ORDINARY && item.isPlain &&
                    prev != null && prev.type == AtomType.ORDINARY && prev.isPlain && !prev.hasScripts
                ) {
                    prev.fuse(item)
                    return@forEach
                }
                preprocessed.add(item)
            }
            return preprocessed
        }

        private fun isNotBinaryOperator(prev: Item?): Boolean =
            prev == null ||
                prev.type in
                setOf(
                    AtomType.BINARY_OPERATOR,
                    AtomType.RELATION,
                    AtomType.OPEN,
                    AtomType.PUNCTUATION,
                    AtomType.LARGE_OPERATOR,
                )
    }

    private fun typeset(items: List<Item>): MathBox {
        var prevType: AtomType? = null

        items.forEach { item ->
            val atom = item.atom
            val spacingType: AtomType =
                when (atom) {
                    is MathSpace -> {
                        x += atom.space * styleFont.muUnit
                        return@forEach
                    }

                    is MathStyle -> {
                        setStyle(atom.style)
                        return@forEach
                    }

                    is MathColor -> {
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        val box = createLine(atom.innerList, font, style, cramped)
                        place(box, color = atom.argb)
                        AtomType.ORDINARY
                    }

                    is Radical -> {
                        // Radicals space as ordinaries on their right (rule 16).
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        place(makeRadical(atom))
                        attachScripts(item, delta = 0f)
                        AtomType.RADICAL
                    }

                    is Fraction -> {
                        addInterElementSpace(prevType, AtomType.FRACTION)
                        place(makeFraction(atom))
                        attachScripts(item, delta = 0f)
                        AtomType.FRACTION
                    }

                    is LargeOperator -> {
                        addInterElementSpace(prevType, AtomType.LARGE_OPERATOR)
                        makeLargeOperator(item, atom)
                        AtomType.LARGE_OPERATOR
                    }

                    is Inner -> {
                        addInterElementSpace(prevType, AtomType.INNER)
                        val box =
                            if (atom.leftBoundary != null || atom.rightBoundary != null) {
                                makeLeftRight(atom)
                            } else {
                                createLine(atom.innerList, font, style, cramped)
                            }
                        place(box)
                        attachScripts(item, delta = 0f)
                        AtomType.INNER
                    }

                    is Underline -> {
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        place(makeUnderline(atom))
                        attachScripts(item, delta = 0f)
                        AtomType.ORDINARY
                    }

                    is Overline -> {
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        place(makeOverline(atom))
                        attachScripts(item, delta = 0f)
                        AtomType.ORDINARY
                    }

                    is Accent -> {
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        val (box, scriptsMoved) = makeAccent(item, atom)
                        place(box)
                        if (!scriptsMoved) {
                            attachScripts(item, delta = 0f)
                        }
                        AtomType.ORDINARY
                    }

                    is MathTable -> {
                        addInterElementSpace(prevType, AtomType.INNER)
                        place(makeTable(atom))
                        AtomType.INNER
                    }

                    is SizedDelimiter -> {
                        addInterElementSpace(prevType, item.type)
                        place(boundaryGlyph(atom.delimiter, atom.height * styleFont.fontSize))
                        attachScripts(item, delta = 0f)
                        item.type
                    }

                    is Stack -> {
                        addInterElementSpace(prevType, item.type)
                        place(makeStack(atom))
                        attachScripts(item, delta = 0f)
                        item.type
                    }

                    is Framed -> {
                        addInterElementSpace(prevType, AtomType.ORDINARY)
                        place(makeFramed(atom))
                        attachScripts(item, delta = 0f)
                        AtomType.ORDINARY
                    }

                    else -> {
                        addInterElementSpace(prevType, item.type)
                        place(glyphRun(item.nucleus))

                        if (item.hasScripts) {
                            // Superscripts sit after the italic correction of the last glyph.
                            val delta =
                                item.nucleus
                                    .codePoints()
                                    .toArray()
                                    .lastOrNull()
                                    ?.let { styleFont.italicCorrection(styleFont.glyph(it)) }
                                    ?: 0f
                            if (delta > 0 && item.subscript == null) {
                                x += delta
                            }
                            attachScripts(item, delta)
                        }
                        item.type
                    }
                }

            prevType = spacingType
        }

        val box = MathBox.containing(children)
        if (spaced && prevType != null) {
            return box.withWidth(box.width + interElementSpace(prevType, AtomType.CLOSE))
        }
        return box
    }

    private fun place(box: MathBox, color: Int? = null) {
        children.add(MathNode.Child(x = x, y = 0f, box = box, color = color))
        x += box.width
    }

    private val lastPlaced: MathBox?
        get() = (children.lastOrNull() as? MathNode.Child)?.box

    private fun setStyle(value: LineStyle) {
        style = value
        styleFont = font.withSize(styleSize(value))
    }

    private fun styleSize(style: LineStyle): Float =
        when (style) {
            LineStyle.DISPLAY, LineStyle.TEXT -> font.fontSize
            LineStyle.SCRIPT -> font.fontSize * font.scriptScaleDown
            LineStyle.SCRIPT_SCRIPT -> font.fontSize * font.scriptScriptScaleDown
        }

    // Spacing

    private fun addInterElementSpace(prevType: AtomType?, currentType: AtomType) {
        x +=
            when {
                prevType != null -> interElementSpace(prevType, currentType)
                // The first atom of a spaced list is spaced as though it follows an open.
                spaced -> interElementSpace(AtomType.OPEN, currentType)
                else -> 0f
            }
    }

    private fun interElementSpace(left: AtomType, right: AtomType): Float {
        val multiplier =
            when (MathSpacing.spaceBetween(left, right)) {
                MathSpacing.Space.NONE -> 0
                MathSpacing.Space.THIN -> 3
                MathSpacing.Space.NS_THIN -> if (style < LineStyle.SCRIPT) 3 else 0
                MathSpacing.Space.NS_MEDIUM -> if (style < LineStyle.SCRIPT) 4 else 0
                MathSpacing.Space.NS_THICK -> if (style < LineStyle.SCRIPT) 5 else 0
            }
        return multiplier * styleFont.muUnit
    }

    // Scripts

    private fun scriptStyle(): LineStyle =
        when (style) {
            LineStyle.DISPLAY, LineStyle.TEXT -> LineStyle.SCRIPT
            LineStyle.SCRIPT, LineStyle.SCRIPT_SCRIPT -> LineStyle.SCRIPT_SCRIPT
        }

    private fun superscriptShiftUp(): Float =
        if (cramped) styleFont.superscriptShiftUpCramped else styleFont.superscriptShiftUp

    /**
     * Places the scripts of [item] after the box just placed. [delta] is the italic correction
     * that offsets the superscript.
     */
    private fun attachScripts(item: Item, delta: Float) {
        val subscriptList = item.subscript
        val superscriptList = item.superscript
        if (subscriptList == null && superscriptList == null) {
            return
        }

        val base = lastPlaced ?: MathBox.EMPTY
        val scriptFont = font.withSize(styleSize(scriptStyle()))

        var superscriptShiftUp = base.ascent - scriptFont.superscriptBaselineDropMax
        var subscriptShiftDown = base.descent + scriptFont.subscriptBaselineDropMin

        if (superscriptList == null && subscriptList != null) {
            val subscript = createLine(subscriptList, font, scriptStyle(), cramped = true)
            subscriptShiftDown = maxOf(subscriptShiftDown, styleFont.subscriptShiftDown)
            subscriptShiftDown = maxOf(subscriptShiftDown, subscript.ascent - styleFont.subscriptTopMax)
            children.add(MathNode.Child(x = x, y = -subscriptShiftDown, box = subscript))
            x += subscript.width + styleFont.spaceAfterScript
            return
        }

        val superscript = createLine(superscriptList!!, font, scriptStyle(), cramped)
        superscriptShiftUp = maxOf(superscriptShiftUp, superscriptShiftUp())
        superscriptShiftUp = maxOf(superscriptShiftUp, superscript.descent + styleFont.superscriptBottomMin)

        if (subscriptList == null) {
            children.add(MathNode.Child(x = x, y = superscriptShiftUp, box = superscript))
            x += superscript.width + styleFont.spaceAfterScript
            return
        }

        val subscript = createLine(subscriptList, font, scriptStyle(), cramped = true)
        subscriptShiftDown = maxOf(subscriptShiftDown, styleFont.subscriptShiftDown)

        val gap = (superscriptShiftUp - superscript.descent) + (subscriptShiftDown - subscript.ascent)
        if (gap < styleFont.subSuperscriptGapMin) {
            subscriptShiftDown += styleFont.subSuperscriptGapMin - gap
            val bottomDelta = styleFont.superscriptBottomMaxWithSubscript - (superscriptShiftUp - superscript.descent)
            if (bottomDelta > 0) {
                superscriptShiftUp += bottomDelta
                subscriptShiftDown -= bottomDelta
            }
        }

        children.add(MathNode.Child(x = x + delta, y = superscriptShiftUp, box = superscript))
        children.add(MathNode.Child(x = x, y = -subscriptShiftDown, box = subscript))
        x += maxOf(superscript.width + delta, subscript.width) + styleFont.spaceAfterScript
    }

    // Fractions

    private fun makeFraction(fraction: Fraction): MathBox {
        val fractionStyle =
            when (style) {
                LineStyle.DISPLAY -> LineStyle.TEXT
                LineStyle.TEXT -> LineStyle.SCRIPT
                LineStyle.SCRIPT, LineStyle.SCRIPT_SCRIPT -> LineStyle.SCRIPT_SCRIPT
            }
        val numerator = createLine(fraction.numerator, font, fractionStyle, cramped = false)
        val denominator = createLine(fraction.denominator, font, fractionStyle, cramped = true)
        val display = style == LineStyle.DISPLAY
        val metrics = styleFont

        var numeratorShiftUp =
            when {
                fraction.hasRule && display -> metrics.fractionNumeratorDisplayStyleShiftUp
                fraction.hasRule -> metrics.fractionNumeratorShiftUp
                display -> metrics.stackTopDisplayStyleShiftUp
                else -> metrics.stackTopShiftUp
            }
        var denominatorShiftDown =
            when {
                fraction.hasRule && display -> metrics.fractionDenominatorDisplayStyleShiftDown
                fraction.hasRule -> metrics.fractionDenominatorShiftDown
                display -> metrics.stackBottomDisplayStyleShiftDown
                else -> metrics.stackBottomShiftDown
            }
        val barLocation = metrics.axisHeight
        val barThickness = if (fraction.hasRule) metrics.fractionRuleThickness else 0f

        if (fraction.hasRule) {
            val numeratorGap = (numeratorShiftUp - numerator.descent) - (barLocation + barThickness / 2)
            val minNumeratorGap = if (display) metrics.fractionNumeratorDisplayStyleGapMin else metrics.fractionNumeratorGapMin
            if (numeratorGap < minNumeratorGap) {
                numeratorShiftUp += minNumeratorGap - numeratorGap
            }

            val denominatorGap = (barLocation - barThickness / 2) - (denominator.ascent - denominatorShiftDown)
            val minDenominatorGap = if (display) metrics.fractionDenominatorDisplayStyleGapMin else metrics.fractionDenominatorGapMin
            if (denominatorGap < minDenominatorGap) {
                denominatorShiftDown += minDenominatorGap - denominatorGap
            }
        } else {
            val clearance = (numeratorShiftUp - numerator.descent) - (denominator.ascent - denominatorShiftDown)
            val minGap = if (display) metrics.stackDisplayStyleGapMin else metrics.stackGapMin
            if (clearance < minGap) {
                numeratorShiftUp += (minGap - clearance) / 2
                denominatorShiftDown += (minGap - clearance) / 2
            }
        }

        val width = maxOf(numerator.width, denominator.width)
        val nodes =
            buildList {
                add(MathNode.Child(x = (width - numerator.width) / 2, y = numeratorShiftUp, box = numerator))
                add(MathNode.Child(x = (width - denominator.width) / 2, y = -denominatorShiftDown, box = denominator))
                if (barThickness > 0) {
                    add(MathNode.Rule(x = 0f, y = barLocation - barThickness / 2, width = width, height = barThickness))
                }
            }
        val box =
            MathBox(
                width = width,
                ascent = numerator.ascent + numeratorShiftUp,
                descent = denominator.descent + denominatorShiftDown,
                children = nodes,
            )

        if (fraction.leftDelimiter == null && fraction.rightDelimiter == null) {
            return box
        }

        val delimiterHeight = if (display) metrics.fractionDelimiterDisplayStyleSize else metrics.fractionDelimiterSize
        return row(
            listOfNotNull(
                fraction.leftDelimiter?.takeIf { it.isNotEmpty() }?.let { boundaryGlyph(it, delimiterHeight) },
                box,
                fraction.rightDelimiter?.takeIf { it.isNotEmpty() }?.let { boundaryGlyph(it, delimiterHeight) },
            ),
        )
    }

    // Radicals

    private fun makeRadical(radical: Radical): MathBox {
        val inner = createLine(radical.radicand, font, style, cramped = true)
        val metrics = styleFont
        var clearance = if (style == LineStyle.DISPLAY) metrics.radicalDisplayStyleVerticalGap else metrics.radicalVerticalGap
        val ruleThickness = metrics.radicalRuleThickness
        val radicalHeight = inner.ascent + inner.descent + clearance + ruleThickness

        val glyph = verticalGlyph("√", radicalHeight)

        // OpenType doesn't assume glyph ascent equals the rule thickness the way TeX does, so center
        // the radicand in any extra height.
        val delta = glyph.height - radicalHeight
        if (delta > 0) {
            clearance += delta / 2
        }

        val radicalAscent = ruleThickness + clearance + inner.ascent
        val shiftUp = radicalAscent - glyph.ascent
        val radicalGlyph = glyph.shifted(down = -shiftUp)
        val descent = maxOf(radicalGlyph.ascent + radicalGlyph.descent - radicalAscent, inner.descent)

        var radicalShift = 0f
        val nodes = mutableListOf<MathNode>()
        var ascent = radicalAscent + metrics.radicalExtraAscender

        radical.degree?.let { degreeList ->
            val degree = createLine(degreeList, font, LineStyle.SCRIPT_SCRIPT, cramped = false)
            var kernBefore = metrics.radicalKernBeforeDegree
            val raise = metrics.radicalDegreeBottomRaisePercent * (ascent - descent)
            radicalShift = kernBefore + degree.width + metrics.radicalKernAfterDegree
            if (radicalShift < 0) {
                kernBefore -= radicalShift
                radicalShift = 0f
            }
            nodes.add(MathNode.Child(x = kernBefore, y = raise, box = degree))
            ascent = maxOf(ascent, raise + degree.ascent)
        }

        val radicandX = radicalShift + radicalGlyph.width
        nodes.add(MathNode.Child(x = radicalShift, y = 0f, box = radicalGlyph))
        nodes.add(MathNode.Rule(x = radicandX, y = radicalAscent - ruleThickness, width = inner.width, height = ruleThickness))
        nodes.add(MathNode.Child(x = radicandX, y = 0f, box = inner))

        return MathBox(width = radicandX + inner.width, ascent = ascent, descent = descent, children = nodes)
    }

    // Glyphs

    private fun glyphRun(text: String): MathBox {
        var runX = 0f
        val nodes = mutableListOf<MathNode>()
        var ascent = 0f
        var descent = 0f

        text.codePoints().forEach { codepoint ->
            val glyph = styleFont.glyph(codepoint)
            if (glyph == 0) {
                throw MathParseException("No glyph for U+%04X".format(codepoint))
            }
            val node = glyphNode(glyph, runX, 0f)
            nodes.add(node)
            ascent = maxOf(ascent, node.ascent)
            descent = maxOf(descent, node.descent)
            runX += node.width
        }

        return MathBox(width = runX, ascent = ascent, descent = descent, children = nodes)
    }

    private fun glyphNode(glyph: Int, x: Float, y: Float, width: Float = styleFont.advance(glyph)): MathNode.Glyph =
        MathNode.Glyph(
            x = x,
            y = y,
            glyph = glyph,
            fontSize = styleFont.fontSize,
            width = width,
            ascent = styleFont.ascent(glyph),
            descent = styleFont.descent(glyph),
        )

    private fun glyphBox(glyph: Int, width: Float = styleFont.advance(glyph)): MathBox {
        val node = glyphNode(glyph, 0f, 0f, width)
        return MathBox(width = width, ascent = node.ascent, descent = node.descent, children = listOf(node))
    }

    /**
     * The smallest vertical variant of [character] at least [height] tall, built from parts when
     * no variant is tall enough.
     */
    private fun verticalGlyph(character: String, height: Float): MathBox {
        val base = styleFont.glyph(character.codePointAt(0))
        if (base == 0) {
            throw MathParseException("No glyph for delimiter $character")
        }
        val variants = styleFont.verticalVariants(base)
        val variant =
            variants.firstOrNull { styleFont.ascent(it) + styleFont.descent(it) >= height }
                ?: variants.last()

        if (styleFont.ascent(variant) + styleFont.descent(variant) < height) {
            constructGlyph(base, height)?.let { return it }
        }
        return glyphBox(variant)
    }

    private fun constructGlyph(glyph: Int, height: Float): MathBox? {
        val parts = styleFont.verticalAssembly(glyph)?.takeIf { it.isNotEmpty() } ?: return null
        val (glyphs, offsets, totalHeight) = assembleParts(parts, height)

        val nodes = glyphs.mapIndexed { index, part -> glyphNode(part, 0f, offsets[index]) }
        val width = glyphs.maxOf { styleFont.advance(it) }
        return MathBox(width = width, ascent = totalHeight, descent = 0f, children = nodes)
    }

    /**
     * Repeats the extender parts until the assembly reaches [height], spreading any excess
     * evenly across the connectors.
     */
    private fun assembleParts(parts: List<ScaledGlyphPart>, height: Float): Triple<List<Int>, List<Float>, Float> {
        val minDistance = styleFont.minConnectorOverlap
        var extenders = 0

        while (true) {
            val glyphs = mutableListOf<Int>()
            val offsets = mutableListOf<Float>()
            var prev: ScaledGlyphPart? = null
            var minOffset = 0f
            var maxDelta = Float.MAX_VALUE

            parts.forEach { part ->
                val repeats = if (part.isExtender) extenders else 1
                repeat(repeats) {
                    glyphs.add(part.glyph)
                    prev?.let { previous ->
                        val maxOverlap = minOf(previous.endConnectorLength, part.startConnectorLength)
                        val minOffsetDelta = previous.fullAdvance - maxOverlap
                        val maxOffsetDelta = previous.fullAdvance - minDistance
                        maxDelta = minOf(maxDelta, maxOffsetDelta - minOffsetDelta)
                        minOffset += minOffsetDelta
                    }
                    offsets.add(minOffset)
                    prev = part
                }
            }

            val last = prev
            if (last == null) {
                extenders += 1
                continue
            }

            val minHeight = minOffset + last.fullAdvance
            val maxHeight = minHeight + maxDelta * (glyphs.size - 1)
            if (minHeight >= height || extenders >= MAX_EXTENDERS) {
                return Triple(glyphs, offsets, minHeight)
            }
            if (height <= maxHeight) {
                val increase = (height - minHeight) / (glyphs.size - 1)
                val spread = offsets.mapIndexed { index, offset -> offset + index * increase }
                return Triple(glyphs, spread, spread.last() + last.fullAdvance)
            }
            extenders += 1
        }
    }

    /**
     * A delimiter at least [height] tall, centered on the math axis.
     */
    private fun boundaryGlyph(delimiter: String, height: Float): MathBox {
        val glyph = verticalGlyph(delimiter, height)
        val shiftDown = 0.5f * (glyph.ascent - glyph.descent) - styleFont.axisHeight
        return glyph.shifted(down = shiftDown)
    }

    // Large operators

    private fun makeLargeOperator(item: Item, op: LargeOperator) {
        val limits = op.hasLimits && style == LineStyle.DISPLAY

        if (op.nucleus.codePointCount(0, op.nucleus.length) != 1) {
            addLimits(item, op, glyphRun(op.nucleus), delta = 0f, limits = limits)
            return
        }

        var glyph = styleFont.glyph(op.nucleus.codePointAt(0))
        if (style == LineStyle.DISPLAY && glyph != 0) {
            glyph = styleFont.largerGlyph(glyph)
        }
        val delta = styleFont.italicCorrection(glyph)
        val width =
            if (item.subscript != null && !limits) {
                // Without limits the subscript tucks under the italic correction.
                styleFont.advance(glyph) - delta
            } else {
                styleFont.advance(glyph)
            }
        val box = glyphBox(glyph, width)
        val shiftDown = 0.5f * (box.ascent - box.descent) - styleFont.axisHeight
        addLimits(item, op, box.shifted(down = shiftDown), delta = delta, limits = limits)
    }

    private fun addLimits(item: Item, op: LargeOperator, nucleus: MathBox, delta: Float, limits: Boolean) {
        if (!item.hasScripts) {
            place(nucleus)
            return
        }
        if (!limits) {
            place(nucleus)
            attachScripts(item, delta)
            return
        }

        val upper = item.superscript?.let { createLine(it, font, scriptStyle(), cramped) }
        val lower = item.subscript?.let { createLine(it, font, scriptStyle(), cramped = true) }
        val upperGap = upper?.let { maxOf(styleFont.upperLimitGapMin, styleFont.upperLimitBaselineRiseMin - it.descent) } ?: 0f
        val lowerGap = lower?.let { maxOf(styleFont.lowerLimitGapMin, styleFont.lowerLimitBaselineDropMin - it.ascent) } ?: 0f
        place(limitsBox(nucleus, upper, lower, upperGap, lowerGap, shift = delta / 2))
    }

    private fun limitsBox(
        nucleus: MathBox,
        upper: MathBox?,
        lower: MathBox?,
        upperGap: Float,
        lowerGap: Float,
        shift: Float,
    ): MathBox {
        val width = maxOf(nucleus.width, upper?.width ?: 0f, lower?.width ?: 0f)
        val nodes = mutableListOf<MathNode>(MathNode.Child(x = (width - nucleus.width) / 2, y = 0f, box = nucleus))
        var ascent = nucleus.ascent
        var descent = nucleus.descent

        upper?.let {
            nodes.add(MathNode.Child(x = shift + (width - it.width) / 2, y = nucleus.ascent + upperGap + it.descent, box = it))
            ascent += upperGap + it.ascent + it.descent
        }
        lower?.let {
            nodes.add(MathNode.Child(x = -shift + (width - it.width) / 2, y = -(nucleus.descent + lowerGap + it.ascent), box = it))
            descent += lowerGap + it.ascent + it.descent
        }

        return MathBox(width = width, ascent = ascent, descent = descent, children = nodes)
    }

    // Delimiters

    private fun makeLeftRight(inner: Inner): MathBox {
        val innerBox = createLine(inner.innerList, font, style, cramped, spaced = true)
        val axisHeight = styleFont.axisHeight
        val delta = maxOf(innerBox.ascent - axisHeight, innerBox.descent + axisHeight)
        // Cover at least 90.1% of the formula, or fall at most 5pt (half an em) short, as plain.tex.
        val coverage = (delta / 500f) * DELIMITER_FACTOR
        val shortfall = 2f * delta - DELIMITER_SHORTFALL_EM * styleFont.fontSize
        val height = maxOf(coverage, shortfall)

        return row(
            listOfNotNull(
                inner.leftBoundary?.takeIf { it.isNotEmpty() }?.let { boundaryGlyph(it, height) },
                innerBox,
                inner.rightBoundary?.takeIf { it.isNotEmpty() }?.let { boundaryGlyph(it, height) },
            ),
        )
    }

    // Over and under lines

    private fun makeUnderline(underline: Underline): MathBox {
        val inner = createLine(underline.innerList, font, style, cramped)
        val thickness = styleFont.underbarRuleThickness
        val ruleTop = -(inner.descent + styleFont.underbarVerticalGap)
        return MathBox(
            width = inner.width,
            ascent = inner.ascent,
            descent = inner.descent + styleFont.underbarVerticalGap + thickness + styleFont.underbarExtraDescender,
            children =
                listOf(
                    MathNode.Child(x = 0f, y = 0f, box = inner),
                    MathNode.Rule(x = 0f, y = ruleTop - thickness, width = inner.width, height = thickness),
                ),
        )
    }

    private fun makeOverline(overline: Overline): MathBox {
        val inner = createLine(overline.innerList, font, style, cramped = true)
        val thickness = styleFont.overbarRuleThickness
        val ruleBottom = inner.ascent + styleFont.overbarVerticalGap
        return MathBox(
            width = inner.width,
            ascent = ruleBottom + thickness + styleFont.overbarExtraAscender,
            descent = inner.descent,
            children =
                listOf(
                    MathNode.Child(x = 0f, y = 0f, box = inner),
                    MathNode.Rule(x = 0f, y = ruleBottom, width = inner.width, height = thickness),
                ),
        )
    }

    // Accents

    /**
     * Returns the accent box and whether the accent's scripts were moved onto a single-character
     * accentee, as TeX does.
     */
    private fun makeAccent(item: Item, accent: Accent): Pair<MathBox, Boolean> {
        var accentee = createLine(accent.innerList, font, style, cramped = true)
        if (accent.nucleus.isEmpty()) {
            return accentee to false
        }

        val accenteeWidth = accentee.width
        val accentGlyph = horizontalVariant(styleFont.glyph(accent.nucleus.codePointAt(0)), accenteeWidth)
        val delta = minOf(accentee.ascent, styleFont.accentBaseHeight)

        val single = singleCharAccentee(accent)
        val accenteeAdjustment =
            if (single == null) {
                accenteeWidth / 2
            } else {
                styleFont.topAccentAdjustment(styleFont.glyph(styledNucleus(single).codePointAt(0)))
            }
        val skew = accenteeAdjustment - styleFont.topAccentAdjustment(accentGlyph)
        val height = accentee.ascent - delta

        var scriptsMoved = false
        if (single != null && single::class == MathAtom::class && item.hasScripts) {
            val withScripts =
                MathAtom(single.type, single.nucleus).also {
                    it.fontStyle = single.fontStyle
                    it.superscript = item.superscript
                    it.subscript = item.subscript
                }
            accentee = createLine(mutableListOf(withScripts), font, style, cramped)
            scriptsMoved = true
        }

        val accentBox = glyphBox(accentGlyph)
        val box =
            MathBox(
                width = accentee.width,
                ascent = maxOf(accentee.ascent, accentee.ascent - delta + accentBox.ascent),
                descent = accentee.descent,
                children =
                    listOf(
                        MathNode.Child(x = 0f, y = 0f, box = accentee),
                        MathNode.Child(x = skew, y = height, box = accentBox),
                    ),
            )
        return box to scriptsMoved
    }

    private fun singleCharAccentee(accent: Accent): MathAtom? {
        val inner = accent.innerList.singleOrNull() ?: return null
        if (inner.nucleus.codePointCount(0, inner.nucleus.length) != 1 || inner.hasScripts) {
            return null
        }
        return inner
    }

    private fun styledNucleus(atom: MathAtom): String =
        if (atom.type == AtomType.VARIABLE || atom.type == AtomType.NUMBER) {
            MathCharset.styled(atom.nucleus, atom.fontStyle)
        } else {
            atom.nucleus
        }

    /**
     * The widest horizontal variant of [glyph] no wider than [maxWidth], or [glyph] itself.
     */
    private fun horizontalVariant(glyph: Int, maxWidth: Float): Int {
        var current = glyph
        styleFont.horizontalVariants(glyph).forEach { variant ->
            if (styleFont.inkWidth(variant) > maxWidth) {
                return current
            }
            current = variant
        }
        return current
    }

    // Stacks and frames

    private fun makeStack(stack: Stack): MathBox {
        val base = createLine(stack.base, font, style, cramped)
        val over = stack.over?.let { createLine(it, font, scriptStyle(), cramped) }
        val under = stack.under?.let { createLine(it, font, scriptStyle(), cramped = true) }
        val overGap = over?.let { maxOf(styleFont.upperLimitGapMin, styleFont.upperLimitBaselineRiseMin - it.descent) } ?: 0f
        val underGap = under?.let { maxOf(styleFont.lowerLimitGapMin, styleFont.lowerLimitBaselineDropMin - it.ascent) } ?: 0f
        return limitsBox(base, over, under, overGap, underGap, shift = 0f)
    }

    private fun makeFramed(framed: Framed): MathBox {
        val inner = createLine(framed.innerList, font, style, cramped)
        val padding = FRAME_PADDING_EM * styleFont.fontSize
        val rule = FRAME_RULE_EM * styleFont.fontSize
        val width = inner.width + 2 * (padding + rule)
        val top = inner.ascent + padding
        val bottom = -(inner.descent + padding + rule)
        val height = top + rule - bottom

        return MathBox(
            width = width,
            ascent = top + rule,
            descent = -bottom,
            children =
                listOf(
                    MathNode.Child(x = padding + rule, y = 0f, box = inner),
                    MathNode.Rule(x = 0f, y = bottom, width = width, height = rule),
                    MathNode.Rule(x = 0f, y = top, width = width, height = rule),
                    MathNode.Rule(x = 0f, y = bottom, width = rule, height = height),
                    MathNode.Rule(x = width - rule, y = bottom, width = rule, height = height),
                ),
        )
    }

    // Tables

    private fun makeTable(table: MathTable): MathBox {
        val numColumns = table.numColumns
        if (numColumns == 0 || table.numRows == 0) {
            return MathBox.EMPTY
        }

        val columnWidths = FloatArray(numColumns)
        val cells =
            table.cells.map { row ->
                row.mapIndexed { column, cell ->
                    createLine(cell, font, style, cramped = false).also {
                        columnWidths[column] = maxOf(columnWidths[column], it.width)
                    }
                }
            }

        val rows =
            cells.map { row ->
                var columnStart = 0f
                val nodes =
                    row.mapIndexed { column, cell ->
                        if (column > 0) {
                            columnStart += columnWidths[column - 1] + table.gapBefore(column) * styleFont.muUnit
                        }
                        val offset =
                            when (table.alignment(column)) {
                                ColumnAlignment.LEFT -> 0f
                                ColumnAlignment.CENTER -> (columnWidths[column] - cell.width) / 2
                                ColumnAlignment.RIGHT -> columnWidths[column] - cell.width
                            }
                        MathNode.Child(x = columnStart + offset, y = 0f, box = cell)
                    }
                MathBox.containing(nodes)
            }

        return MathBox.containing(positionRows(rows, table))
    }

    /**
     * Stacks rows with TeX's baselineskip rules, then centers the table on the math axis.
     */
    private fun positionRows(rows: List<MathBox>, table: MathTable): List<MathNode> {
        val fontSize = styleFont.fontSize
        val openup = table.interRowAdditionalSpacing * JOT_MULTIPLIER * fontSize
        val baselineSkip = openup + BASELINE_SKIP_MULTIPLIER * fontSize
        val lineSkip = openup + LINE_SKIP_MULTIPLIER * fontSize
        val lineSkipLimit = openup

        var position = 0f
        var prevDescent = 0f
        val offsets =
            rows.mapIndexed { index, row ->
                if (index > 0) {
                    var skip = baselineSkip
                    if (skip - (prevDescent + row.ascent) < lineSkipLimit) {
                        skip = prevDescent + row.ascent + lineSkip
                    }
                    position -= skip
                }
                prevDescent = row.descent
                position
            }

        val ascent = rows.first().ascent
        val descent = -position + prevDescent
        val shiftDown = 0.5f * (ascent - descent) - styleFont.axisHeight

        return rows.mapIndexed { index, row ->
            MathNode.Child(x = 0f, y = offsets[index] - shiftDown, box = row)
        }
    }

    private fun row(boxes: List<MathBox>): MathBox {
        var rowX = 0f
        val nodes =
            boxes.map { box ->
                MathNode.Child(x = rowX, y = 0f, box = box).also { rowX += box.width }
            }
        return MathBox.containing(nodes)
    }

    /**
     * An atom with its spacing type and final nucleus, so layout never mutates parsed atoms.
     */
    private class Item(val atom: MathAtom) {
        var type: AtomType = atom.type
        var nucleus: String = atom.nucleus
        var subscript: MathList? = atom.subscript
        var superscript: MathList? = atom.superscript

        val isPlain: Boolean
            get() = atom::class == MathAtom::class

        val hasScripts: Boolean
            get() = subscript != null || superscript != null

        fun fuse(other: Item) {
            nucleus += other.nucleus
            subscript = other.subscript
            superscript = other.superscript
        }
    }
}

private const val DELIMITER_FACTOR = 901
private const val DELIMITER_SHORTFALL_EM = 0.5f
private const val BASELINE_SKIP_MULTIPLIER = 1.2f
private const val LINE_SKIP_MULTIPLIER = 0.1f
private const val JOT_MULTIPLIER = 0.3f
private const val FRAME_PADDING_EM = 0.3f
private const val FRAME_RULE_EM = 0.04f
private const val MAX_EXTENDERS = 64
