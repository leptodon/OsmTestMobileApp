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
    val locationStateFlow = MutableStateFlow(Location(LocationManager.GPS_PROVIDER))
    val gpsProviderState = MutableLiveData(false)
    private val locHandlerThread = HandlerThread("LocationHandlerThread")

    init {
        startLocation()
    }

    fun startLocation(minTimeMs: Long = MIN_TIME_MS, minDistanceM: Float = MIN_DISTANCE_M) {
        locationListener().let {
            locationListener = it
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, it, locHandlerThread.looper)
        }
        gpsProviderState.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun locationListener() = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationStateFlow.value = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            //Logger.i("onStatusChanged $provider $status $extras")
        }

        override fun onProviderEnabled(provider: String) {
            //Logger.i("onProviderEnabled $provider")
            gpsProviderState.value = true
        }

        override fun onProviderDisabled(provider: String) {
            //Logger.i("onProviderDisabled $provider")
            gpsProviderState.value = false
        }
    }
}