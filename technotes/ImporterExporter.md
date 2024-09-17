## Imports

- Foreground process in a viewmodel, let the lifecycle handle configuration changes
- Show progress bar
- Collect failed feeds and display the reason to the user

See [PocketCast's OPML import task](https://github.com/Automattic/pocket-casts-android/blob/76e40d3e5eb07d6243e788d04d990597ceb9c16f/modules/services/repositories/src/main/java/au/com/shiftyjelly/pocketcasts/repositories/opml/OpmlImportTask.kt#L248) which uses the `setForeground` setting******

Resurrect the picker code to grab a URI

```kotlin
 val picker = rememberLauncherForActivityResult(
	ActivityResultContracts.GetContent()
) { uri ->
	if (uri != null) {
		context.contentResolver.openInputStream(uri).use { inputStream ->
			viewModel.importOPML(inputStream) {
				// success
			}
		}
	}
}

picker.launch("text/xml")
```

## Exports

- Re-add `Account#opmlFile` method.
- Would be nice to memoize but ideally it's used as a one-shot
- Existing logic applies via [previous implementation](./DeletedScenes.md)
