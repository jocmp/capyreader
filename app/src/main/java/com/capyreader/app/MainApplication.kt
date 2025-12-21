package com.capyreader.app

import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
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

class MainApplication : Application(), ImageLoaderFactory {
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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(ImageDecoderDecoder.Factory())
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .okHttpClient {
                baseHttpClient()
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
