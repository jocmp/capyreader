# Refresh

You can specify a refresh rate for Capy Reader ranging from 15 minutes to every 8 hours. This refresh rate is not guarunteed since Android's job scheduler may prioritize the refresh job to a lower bucket based on usage.

## Reference

- [Optimize battery use for task scheduling APIs](https://developer.android.com/develop/background-work/background-tasks/optimize-battery)
- [Power management restrictions](https://developer.android.com/topic/performance/power/power-details)
- [Debug WorkManager](https://developer.android.com/develop/background-work/background-tasks/testing/persistent/debug)
- [App Standby Buckets](https://developer.android.com/topic/performance/appstandby)

> To check which bucket your app is assigned to,... Run the following command in a terminal window:
> The system throttles your app whenever it's placed in an App Standby Bucket whose value is greater than STANDBY_BUCKET_ACTIVE (10).

```bash
adb shell am get-standby-bucket com.jocmp.capyreader
```
