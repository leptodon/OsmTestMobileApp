package ru.cactus.mapapp.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.dsl.module

val locationModule = module { single { locationProvider(get()) } }

private fun locationProvider(context: Context): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(context)
}