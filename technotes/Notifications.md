```kotlin
if (BuildConfig.DEBUG) {  
    val context = LocalContext.current  
    Button(onClick = {  
        val request = OneTimeWorkRequestBuilder<RefreshFeedsWorker>()  
            .build()  
  
        WorkManager.getInstance(context).enqueue(request)  
    }) {  
        Text("BG WORK")  
    }  
}
```