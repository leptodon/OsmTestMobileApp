package ru.cactus.mapapp

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient

@SuppressLint("MissingPermission")
class MainViewModel(
    private val locationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    fun setupLocation() {
        locationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                _location.postValue(location)
            }
    }


}