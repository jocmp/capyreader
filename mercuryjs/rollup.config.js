import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import commonjs from '@rollup/plugin-commonjs';
import nodeResolve from '@rollup/plugin-node-resolve';
import terser from '@rollup/plugin-terser';

const require = createRequire(import.meta.url);

// The web build defines a global `Mercury` and parses with cheerio. The ESM
// build still bundles jQuery, which needs a real document.
const mercury = readFileSync(
  require.resolve('@jocmp/mercury-parser/dist/mercury.web.js'),
  'utf8'
).replace(/\n\/\/# sourceMappingURL=.*$/, '');

// JavaScriptSandbox is a bare V8 isolate: no DOM, no network, no URL.
// Capy fetches the HTML in Kotlin and hands it to `capyMercury.parse`.
// src/ runs first to install globals, then the Mercury script follows.
export default {
  input: 'src/index.js',
  plugins: [
    nodeResolve({ browser: true, preferBuiltins: false }),
    commonjs(),
    terser(),
  ],
  output: {
    file: '../app/src/main/assets/mercury/mercury-headless.js',
    format: 'iife',
    footer: mercury,
  },
};
