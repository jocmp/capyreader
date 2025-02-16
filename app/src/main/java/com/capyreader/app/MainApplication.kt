package com.capyreader.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.capyreader.app.preferences.AppPreferences
import com.google.android.material.color.DynamicColors
import com.jocmp.capy.UserAgentInterceptor
import okhttp3.OkHttpClient
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

        if (get<AppPreferences>().accountID.get().isNotBlank()) {
            loadAccountModules()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(ImageDecoderDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(UserAgentInterceptor())
                    .build()
            }
            .build()
    }
}
