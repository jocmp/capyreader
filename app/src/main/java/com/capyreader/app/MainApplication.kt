package com.capyreader.app

import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import coil3.video.VideoFrameDecoder
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.widget.HeadlinesWidgetReceiver
import com.google.android.material.color.DynamicColors
import com.jocmp.capy.accounts.baseHttpClient
import com.jocmp.capy.common.launchUI
import kotlinx.coroutines.MainScope
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MainApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        startKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            setupCommonModules()
        }

        if (get<AppPreferences>().isLoggedIn) {
            loadAccountModules()
        }

       loadWidgetPreview()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { baseHttpClient() }))
                add(AnimatedImageDecoder.Factory())
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    /**
     * [Docs](https://developer.android.com/develop/ui/compose/glance/generated-previews)
     */
    private fun loadWidgetPreview() {
        MainScope().launchUI {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                GlanceAppWidgetManager(applicationContext).setWidgetPreviews(HeadlinesWidgetReceiver::class)
            }
        }
    }
}
