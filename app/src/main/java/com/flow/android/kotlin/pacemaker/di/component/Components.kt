package com.flow.android.kotlin.pacemaker.di.component

import android.app.Application
import com.flow.android.kotlin.pacemaker.di.module.*
import com.flow.android.kotlin.pacemaker.application.MainApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    AndroidInjectionModule::class,
    ActivityModule::class,
    FragmentModule::class,
    ViewModelFactoryModule::class,
    ViewModelModule::class
])
interface ApplicationComponent: AndroidInjector<MainApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ApplicationComponent
    }

    override fun inject(mainApplication: MainApplication)
}