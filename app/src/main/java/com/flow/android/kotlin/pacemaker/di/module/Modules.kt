package com.flow.android.kotlin.pacemaker.di.module

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.flow.android.kotlin.pacemaker.di.scope.ActivityScope
import com.flow.android.kotlin.pacemaker.di.scope.FragmentScope
import com.flow.android.kotlin.pacemaker.model.database.LocalDatabase
import com.flow.android.kotlin.pacemaker.repository.Repository
import com.flow.android.kotlin.pacemaker.view.calendar.CalendarFragment
import com.flow.android.kotlin.pacemaker.view.main.MainActivity
import com.flow.android.kotlin.pacemaker.view.settings.SettingsFragment
import com.flow.android.kotlin.pacemaker.view.today.TodayFragment
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import com.flow.android.kotlin.pacemaker.view_model.ViewModelFactory
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
class ApplicationModule {

    @Provides
    @Singleton
    internal fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    internal fun provideLocalDatabase(application: Application): LocalDatabase {
        return Room.databaseBuilder(application, LocalDatabase::class.java, LocalDatabase.name).build()
    }

    @Provides
    @Singleton
    internal fun provideRepository(
        localDatabase: LocalDatabase
    ): Repository {
        return Repository(localDatabase)
    }
}

@Module
abstract class ActivityModule {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity
}

@Module
abstract class FragmentModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun calendarFragment(): CalendarFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun todayFragment(): TodayFragment
}

@Module abstract class ViewModelFactoryModule {

    @Binds
    internal abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel
}

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)