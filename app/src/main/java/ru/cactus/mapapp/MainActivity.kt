package ru.cactus.mapapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.cactus.mapapp.databinding.ActivityMainBinding
import ru.cactus.mapapp.databinding.BottomSheetLayoutBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    companion object {
        private const val COARSE_LOCATION_CODE = 100
        private const val FINE_LOCATION_CODE = 101
    }

    private lateinit var map: MapView

    private val viewModel: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetLayoutBinding

    private var mapController: IMapController? = null
    private var markers: MutableList<Marker> = mutableListOf()
    private var currentMarkerIndex: Int = 0
    private var isFirstStart: Boolean = true

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        map = binding.mapview
        map.isClickable = true
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(false)

        mapController = map.controller

        getInstance().load(this, getPreferences(MODE_PRIVATE))

        with(binding) {
            btnZoomIn.setOnClickListener { mapController?.zoomIn() }
            btnZoomOut.setOnClickListener { mapController?.zoomOut() }

            btnNextTracker.setOnClickListener {
                if (markers.isNotEmpty()) {
                    if (currentMarkerIndex == markers.size - 1) {
                        currentMarkerIndex = 0
                    }
                    mapController?.setCenter(markers[currentMarkerIndex].position)
                    markers[currentMarkerIndex].showInfoWindow()
                    currentMarkerIndex++
                    dialog()
                }
            }
        }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        val bitmapArrow =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_my_tracker_46dp, theme)?.toBitmap()
        locationOverlay.setDirectionIcon(bitmapArrow)
        locationOverlay.enableMyLocation()

        binding.btnCurrentLocation.setOnClickListener {
            locationOverlay.enableFollowLocation()
        }

        map.overlays.add(locationOverlay)

        mapController?.setZoom(9.5)

    }

    override fun onStart() {
        super.onStart()
        checkPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            FINE_LOCATION_CODE
        )
        checkPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            COARSE_LOCATION_CODE
        )
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    private fun permissionGranted() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.locationStateFlow.collect {

                if (it.latitude.toInt() != 0 && isFirstStart) {
                    isFirstStart = false
                    mapController?.setCenter(
                        GeoPoint(
                            it.latitude,
                            it.longitude
                        )
                    )
                    setMockMarkers()
                }
            }
        }
    }

    private fun setMockMarkers() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.locationStateFlow.collect {
                if (markers.isEmpty() && it.latitude.toInt() != 0) {
                    for (location in 0..5) {
                        val marker = Marker(map)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        marker.icon =
                            ResourcesCompat.getDrawable(resources, R.drawable.custom_marker, theme)
                        marker.setOnMarkerClickListener { _, _ ->
                            dialog()
                            true
                        }
                        marker.position = GeoPoint(
                            it.latitude + Random.nextDouble(0.1, 0.9),
                            it.longitude + Random.nextDouble(0.1, 0.9)
                        )

                        marker.infoWindow = MyInfoWindow(map)

                        map.overlays.add(marker)
                        markers.add(marker)
                    }
                }
            }
        }
    }

    private fun dialog() {
        val dialog = BottomSheetDialog(this)
        bottomSheetBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        val bsbView = bottomSheetBinding.root
        dialog.setContentView(bsbView)
        dialog.show()
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            viewModel.startLocation()
            permissionGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startLocation()
                permissionGranted()
            } else {
                showToastPermission("GPS location Permission Denied")
            }
        } else if (requestCode == COARSE_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startLocation()
                permissionGranted()
            } else {
                showToastPermission("Network location Permission Denied")
            }
        }
    }

    private fun showToastPermission(text: String) {
        Toast.makeText(
            this@MainActivity,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

}