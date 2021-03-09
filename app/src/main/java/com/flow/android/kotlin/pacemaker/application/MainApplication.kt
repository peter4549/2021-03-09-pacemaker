package com.flow.android.kotlin.pacemaker.application

import android.app.Application
import com.flow.android.kotlin.pacemaker.di.component.DaggerApplicationComponent
import com.flow.android.kotlin.pacemaker.model.database.LocalDatabase
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class MainApplication: Application(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var localDatabase: LocalDatabase

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DaggerApplicationComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}