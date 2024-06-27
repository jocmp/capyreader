## Imports

- Foreground process in a viewmodel, let the lifecycle handle configuration changes
- Show progress bar
- Collect failed feeds and display the reason to the user

## Exports

- Re-add `Account#opmlFile` method.
- Would be nice to memoize but ideally it's used as a one-shot
- Existing logic applies via [previous implementation](./technotes/DeletedScenes.md)
