package ru.cactus.mapapp

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("MissingPermission")
class MainViewModel(
    private val locationManager: LocationManager
) : ViewModel() {

    companion object {
        const val MIN_TIME_MS: Long = 1000L
        const val MIN_DISTANCE_M: Float = 0.0f
    }

    private var locationListener: LocationListener? = null
    val locationStateFlow = MutableStateFlow(Location(LocationManager.NETWORK_PROVIDER))
    val gpsProviderState = MutableLiveData(false)
    private val locHandlerThread = HandlerThread("LocationHandlerThread")

    init {
        startLocation()
    }

    fun startLocation(
        provider: String = LocationManager.NETWORK_PROVIDER
    ) {
        locationListener().let {
            locationListener = it

            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                it,
                locHandlerThread.looper
            )
        }
        gpsProviderState.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun locationListener() = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location.latitude.toInt() != 0 || location.longitude.toInt() != 0) {
                startLocation(provider = LocationManager.GPS_PROVIDER)
            } else {
                startLocation(provider = LocationManager.NETWORK_PROVIDER)
            }
            locationStateFlow.value = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
            gpsProviderState.value = true
        }

        override fun onProviderDisabled(provider: String) {
            gpsProviderState.value = false
        }
    }
}