// Converts KaTeX's src/fontMetricsData.js into the TSV resource read by FontMetrics.kt.
//
//   node mallet/scripts/katex_metrics.mjs <katex-checkout> > mallet/src/main/resources/com/jocmp/mallet/tex/fontMetricsData.tsv
//
// Each row is: font, UTF-16 code unit, depth, height, italic, skew, width.

import {readFile} from "node:fs/promises";
import path from "node:path";

const katex = process.argv[2];
if (!katex) {
    console.error("usage: katex_metrics.mjs <katex-checkout>");
    process.exit(1);
}

const source = await readFile(path.join(katex, "src/fontMetricsData.js"), "utf8");
const json = source
    .replace(/^\/\/.*$/gm, "")
    .replace(/export default\s*/, "")
    .replace(/;\s*$/, "")
    .replace(/,(\s*[}\]])/g, "$1");
const metrics = JSON.parse(json);

const rows = [];
Object.keys(metrics).sort().forEach((font) => {
    Object.keys(metrics[font])
        .map(Number)
        .sort((a, b) => a - b)
        .forEach((code) => {
            rows.push([font, code, ...metrics[font][code]].join("\t"));
        });
});
process.stdout.write(rows.join("\n") + "\n");
