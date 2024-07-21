
Google Provides downloadable fonts as a part of the AndroidX library. However, this implementation is dependent on GMS to sign each download.


Questions

- How much space does a font take up?
	- 1 or 2MB more for fonts is an acceptable trade-off to avoid the GMS dependency
- Can a font be shared between `/res` and the webview?	

Reference
- https://developer.android.com/develop/ui/compose/text/fonts#variable-fonts
- https://developer.android.com/jetpack/compose/text/fonts#downloadable-fonts
- https://stackoverflow.com/a/4338273
- https://developer.android.com/develop/ui/views/text-and-emoji/fonts-in-xml
