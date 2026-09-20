# mallet

Converts article HTML into a flat list of renderable elements (text with annotation ranges, images, tables, lists, block quotes, audio, video) so the reader can draw it with native Compose components instead of a WebView.

Ported from [Feeder](https://github.com/spacecowboy/Feeder) (`HtmlLinearizer`, renamed `Mallet` here, and its linear model), GPLv3, by Jonas Kalderstam. Android-specific pieces (logging, `ArrayMap`) were replaced with JVM equivalents so the module has no Android dependency.
