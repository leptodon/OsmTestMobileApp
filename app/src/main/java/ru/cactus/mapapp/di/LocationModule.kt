package ru.cactus.mapapp.di

import android.content.Context
import android.location.LocationManager
import org.koin.dsl.module

val locationModule = module { single { locationProvider(get()) } }

private fun locationProvider(context: Context): LocationManager {
    return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}