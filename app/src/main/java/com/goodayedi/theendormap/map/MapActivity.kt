package com.goodayedi.theendormap.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import com.goodayedi.theendormap.R
import com.goodayedi.theendormap.location.LocationData
import com.goodayedi.theendormap.location.LocationLiveData
import com.goodayedi.theendormap.poi.POI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.lang.Exception

private const val REQUEST_LOCATION_PERMISSION_START_UPDATE = 1

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val mapViewModel by viewModels<MapViewModel>()
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var map: GoogleMap
    private var firstLocation = true
    private lateinit var loadingProgressBar: ContentLoadingProgressBar
    private lateinit var userMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)


        val mapOptions = GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .zoomControlsEnabled(true)
            .zoomGesturesEnabled(true)

        val mapFragment = SupportMapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        locationLiveData = LocationLiveData(this)
        locationLiveData.observe(this){
            handleLocationData(it!!)
        }

        mapViewModel.getUiState().observe(this) {
            updateUIState(it!!)
        }
    }

    private fun updateUIState(state: MapUIState) {
        when(state){
            is MapUIState.Error -> {
                loadingProgressBar.hide()
                Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            MapUIState.Loading -> loadingProgressBar.show()
            is MapUIState.POIReady -> {
                loadingProgressBar.hide()
                state.userPOI?.let { POI ->
                    userMarker = addPOItoMapMarker(POI, map)
                }
                state.POIList?.let {
                    for (POI in it){
                        addPOItoMapMarker(POI, map)
                    }
                }
            }
        }
    }

    private fun handleLocationData(locationData: LocationData) {
        if (handleLocationException(locationData.exception)){
            return
        }
        locationData.location?.let {
            if(firstLocation && ::map.isInitialized){
                val latLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9f))
                firstLocation = false
                mapViewModel.loadPOIList(latitude = it.latitude, longitude = it.longitude)
            }
        }
    }

    private fun handleLocationException(exception: Exception?): Boolean {
        exception ?: return false
        when(exception){
            is SecurityException -> checkLocationPermission(
                REQUEST_LOCATION_PERMISSION_START_UPDATE)
            is ResolvableApiException -> registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                locationLiveData.createLocationRequest()
            }.launch(IntentSenderRequest.Builder(exception.resolution).build())
        }
        return true
    }


    private fun checkLocationPermission(requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_START_UPDATE -> locationLiveData.createLocationRequest()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style))
    }

    private fun addPOItoMapMarker(POI: POI, map: GoogleMap): Marker {
        val latLng = LatLng(POI.latitude, POI.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(POI.title)
            .snippet(POI.description)
        if(POI.iconId > 0){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(POI.iconId))
        } else {
            val hue = when(POI.iconColor){
                Color.BLUE -> BitmapDescriptorFactory.HUE_BLUE
                Color.GREEN -> BitmapDescriptorFactory.HUE_GREEN
                Color.YELLOW -> BitmapDescriptorFactory.HUE_YELLOW
                Color.RED -> BitmapDescriptorFactory.HUE_RED
                else -> BitmapDescriptorFactory.HUE_RED
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue    ))
        }
        val marker = map.addMarker(markerOptions)!!
        marker.tag = POI
        return marker
    }
}