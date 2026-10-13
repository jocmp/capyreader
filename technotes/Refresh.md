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

## Background refresh log

Settings > Advanced > Troubleshooting > Background refresh shows whether Android is running the periodic job. It was added after reports like discussion #2295 (Samsung, Android 16), where refreshes stop while the phone is idle and resume once the app is opened.

- **Status** is read when the dialog opens (`RefreshStatusReader`): refresh interval, Wi-Fi only, the `refresher` job's WorkManager state, next scheduled run, last stop reason, the app standby bucket, battery optimization exemption, background restriction, and whether notifications are allowed.
- **History** is written by `RefreshFeedsWorker` to `files/refresh_log.jsonl` (`RefreshLog`, capped at 100 entries): started, refreshed (with the notification count), skipped for Wi-Fi only, failed, and stopped by Android with its stop reason. Each start shows the time since the previous start. Gaps longer than twice the interval are shown in the error color.
- **Copy report** puts a plain-text English report on the clipboard, with the same device header as the crash log export. Users can paste it into a GitHub discussion.

`Account.refresh()` returns a `Result` instead of throwing, so sync errors never reached the worker's `catch` and the worker logged success. `FeedRefresher` now returns a `RefreshOutcome` so the log records those failures. The worker still returns `Result.success()` for them, as before.

### Testing on a device

`cmd jobscheduler run -f` does not start a periodic WorkManager job before its scheduled time: WorkManager logs "Delaying execution ... because it is being executed before schedule" and reschedules it. WorkManager also schedules its jobs in a JobScheduler namespace, so the command needs `-n androidx.work.systemjobscheduler`. To get a real run, set the interval to Manually only and then to any periodic value. The new periodic request runs right away.

```bash
adb shell run-as com.capyreader.app.debug cat files/refresh_log.jsonl
```
