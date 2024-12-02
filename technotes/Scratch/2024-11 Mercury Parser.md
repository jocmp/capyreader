```kotlin
webView.evaluateJavascript(
  """(function test() {
        return "hello";
      })();
  """.trimIndent()) {  
  it
}
```

1. Load placeholder text
2. Fetch full content
3. evaluateJavascript -> Parser(content)