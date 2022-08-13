package ru.cactus.mapapp

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

@SuppressLint("MissingPermission")
class MainViewModel(
    private val locationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    init {
        setupLocation()
    }

    fun setupLocation() {
        locationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                _location.postValue(location)
            }
            .addOnCompleteListener {
                _location.postValue(it.result)
            }

        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnCompleteListener {
                if (it.result != null) _location.postValue(it.result) }
            .addOnSuccessListener {
                    location: Location? ->
                if (location != null) _location.postValue(location)
            }

    }
}