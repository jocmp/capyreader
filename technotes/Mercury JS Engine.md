# Mercury JS Engine

Experiment (2026-10-26, branch `jc/headless-mercury-js`): run the upstream JS Mercury Parser headless on device instead of the Kotlin port in `:mercuryparser`. Conclusion: keep the Kotlin port. The JS bundle is better used as a parity oracle for the port.

## How it works

- `mercuryjs/` bundles `@jocmp/mercury-parser`'s Node build (`dist/mercury.js`) into `app/src/main/assets/mercury/mercury-headless.js` (~589KB). `make mercury-js` rebuilds it byte-identically.
- `postman-request`, `browser-request` and `iconv-lite` are stubbed. Kotlin fetches and decodes the HTML, and the bundle runs with `fetchAllPages: false`.
- A bare V8 isolate has no `URL`, `URLSearchParams` or `atob`. core-js polyfills them. `entities` (via cheerio) needs `atob` or `Buffer` at load time.
- Don't use `dist/mercury.esm.js`: it is a 2022 build that bundles jQuery. Removed upstream in jocmp/mercury-parser#236.
- `SandboxMercuryEngine` (app) evaluates the bundle in `androidx.javascriptengine`. It keeps one isolate warm, caps its heap at 128MB, and falls back to `KotlinMercuryEngine` when the sandbox is unsupported or the isolate dies.

## WebView dependency

JavaScriptSandbox needs no `WebView` instance, but it does need the WebView *provider package*. The V8 isolate runs in the provider's process (`com.google.android.webview:js_sandboxed_process0`), so:

- `JavaScriptSandbox.isSupported()` and the feature flags depend on the installed provider version. We require `JS_FEATURE_PROMISE_RETURN` and `JS_FEATURE_EVALUATE_WITHOUT_TRANSACTION_LIMIT`; WebView 124 and 155 both had them.
- A WebView update kills the sandbox process. On the Pixel 3a, all 10 isolate deaths in one run landed in the ~4.5 minutes before Play committed a WebView update (139 -> 155). A re-run on 155 had none.
- Timings are consistent with V8 running without a JIT (4-6x slower than desktop V8, close to `node --jitless`). Not confirmed by the androidx docs.

## Results

Fixtures are the top-level `.html` files in `mercuryparser/src/test/resources/fixtures` (167). Times are the median of 3 runs after one warm-up, debug build. Raw per-fixture logs were not kept; rerun the benchmark to regenerate them.

### Galaxy A07 AVD (Android 15, 720x1600 @ 260dpi, 4GB, 8 cores, WebView 124, M4 Pro host)

| Engine | p50 | p90 | p99 | max |
|---|---|---|---|---|
| Kotlin port | 45ms | 220ms | 489ms | 1,094ms |
| JS, web-build bundle | 108ms | 541ms | 983ms | 1,024ms |
| JS, Node-build bundle (JS-only run) | 56ms | 121ms | 706ms | 4,237ms |

- The first two rows alternated engines per fixture; the third ran JS alone, so it isn't a like-for-like comparison. Both bundles produce byte-identical output and run at the same speed in desktop V8.
- Cold start: sandbox connect 90-320ms, first parse 80-500ms.
- Content length agreed within 5% on 156 of 167 fixtures.
- The emulator runs at host CPU speed. The first run right after boot was much slower (5.8s connect, 9s outliers) while the system settled. Discard post-boot runs.

### Memory on the AVD (PSS, each engine alone, above a ~181MB test baseline)

| Engine | App process | Sandbox process | Sandbox after parsing |
|---|---|---|---|
| Kotlin port | +24MB median, +55MB peak | none | none |
| JS sandbox | +5MB median, +20MB peak | 78MB median, 130MB peak | 127MB, retained |

The isolate does not leak (Node: retained heap flat over 668 parses), but V8 keeps its heap while idle. A warm isolate holds ~127MB in a second process unless it is closed.

### Pixel 3a (Android 12, Snapdragon 670, 3.6GB, 61-fixture subset, engines alternating)

| Engine | p50 | p90 | max | Over 1s |
|---|---|---|---|---|
| Kotlin port | 266ms | 734ms | 2,071ms | 4 of 51 |
| JS sandbox | 527ms | 1,403ms | 5,699ms | 11 of 51 |

- Excludes the 10 fixtures that hit the WebView update. They parse normally in isolation.
- The 3a is ~7-8x slower than the AVD. Sandbox cold connect took 0.8-1.2s.
- The worst pages are the 1.7-1.9MB Reddit fixtures: 4-5.7s with JS, 1.7-2s with Kotlin.
- A Galaxy A07 (Helio G99, ~727 Geekbench 6 single-core) should land between the AVD and the 3a.

## Why we kept the Kotlin port

The JS engine still needs a fallback for:

- an unsupported WebView;
- isolate deaths;
- WebView updates.

That fallback is the Kotlin port, so the JS route adds a second parser instead of removing one. On low-end hardware it is also ~2x slower, with a worse tail, and it holds memory in a second process. The useful part is `mercuryjs/`: diff its output against the port on the fixtures to catch drift when bumping `@jocmp/mercury-parser`.

## Rerunning

```
make mercury-js
./gradlew :app:assembleFreeDebug :app:assembleFreeDebugAndroidTest
adb install -r -t app/build/outputs/apk/free/debug/app-free-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/free/debug/app-free-debug-androidTest.apk
adb logcat -c
adb shell am instrument -w -e class com.capyreader.app.articles.MercuryEngineBenchmark com.capyreader.app.debug.test/androidx.test.runner.AndroidJUnitRunner
adb logcat -d -s MercuryBench
```

Add `-e engines kotlin` or `-e engines js` to run one engine. Add `-e fixtures a.html,b.html` to run a subset. Rows are tab-separated: `fixture bytes kotlin_ms js_ms kotlin_len js_len`, with `-1` for skipped or failed.
