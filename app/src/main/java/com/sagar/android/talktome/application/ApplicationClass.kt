package com.sagar.android.talktome.application

import android.app.Application
import android.content.SharedPreferences
import com.sagar.android.logutilmaster.LogUtil
import com.sagar.android.talktome.core.KeywordAndConstant
import com.sagar.android.talktome.di.NetworkModule
import com.sagar.android.talktome.repository.Repository
import com.sagar.android.talktome.ui.mainactivity.ViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.BuildConfig
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

@Suppress("unused")
class ApplicationClass : Application(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {

        import(androidXModule(this@ApplicationClass))

        bind() from singleton {
            LogUtil(
                LogUtil.Builder()
                    .setCustomLogTag(KeywordAndConstant.LOG_TAG)
                    .setShouldHideLogInReleaseMode(false, BuildConfig.DEBUG)
            )
        }

        bind() from singleton { NetworkModule(instance()).apiInterface }

        bind() from singleton {
            val pref: SharedPreferences by this.kodein.instance(arg = KeywordAndConstant.SHARED_PREF_DB)
            Repository(
                instance(),
                instance(),
                pref
            )
        }

        bind() from provider { ViewModelProvider(instance()) }
    }

}