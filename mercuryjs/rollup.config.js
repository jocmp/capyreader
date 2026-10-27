import commonjs from '@rollup/plugin-commonjs';
import nodeResolve from '@rollup/plugin-node-resolve';
import terser from '@rollup/plugin-terser';

// Kotlin fetches and decodes the HTML, so Mercury's network and charset
// dependencies are never called.
const NETWORK_MODULES = ['postman-request', 'browser-request', 'iconv-lite'];
const STUB_ID = '\0network-stub';

// JavaScriptSandbox is a bare V8 isolate: no DOM, no network, no URL.
// Capy hands fetched HTML to `capyMercury.parse`.
export default {
  input: 'src/index.js',
  plugins: [
    stubNetworkModules(),
    nodeResolve({ browser: true, preferBuiltins: false }),
    commonjs(),
    terser(),
  ],
  output: {
    file: '../app/src/main/assets/mercury/mercury-headless.js',
    format: 'iife',
  },
};

function stubNetworkModules() {
  return {
    name: 'stub-network-modules',
    resolveId(source) {
      return NETWORK_MODULES.includes(source) ? STUB_ID : null;
    },
    load(id) {
      if (id !== STUB_ID) {
        return null;
      }

      return `export default function stub() {
        throw new Error('Network access is disabled in the headless parser');
      }`;
    },
  };
}
