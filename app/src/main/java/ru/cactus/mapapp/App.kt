package ru.cactus.mapapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.cactus.mapapp.di.locationModule
import ru.cactus.mapapp.di.viewModelModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    locationModule,
                    viewModelModule
                )
            )
        }
    }
}