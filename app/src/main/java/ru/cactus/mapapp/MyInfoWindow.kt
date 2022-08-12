package ru.cactus.mapapp

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow


class MyInfoWindow(
    private val map: MapView
) :
    InfoWindow(R.layout.info_layout, map) {

    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(map)
        mView.setOnClickListener {
            close()
        }
    }

    override fun onClose() {
    }

}