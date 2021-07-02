package com.goodayedi.theendormap

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import timber.log.Timber
import java.lang.Exception

private const val REQUEST_LOCATION_PERMISSION_START_UPDATE = 1

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val mapViewModel by viewModels<MapViewModel>()
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var map: GoogleMap
    private var firstLocation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        return when(state){
            is MapUIState.Error -> {}
            MapUIState.Loading -> {

            }
            is MapUIState.POIReady -> {

            }
        }
    }

    private fun handleLocationData(locationData: LocationData) {
        if (handleLocationException(locationData.exception)){
            return
        }
        locationData.location?.let {
            if(firstLocation){
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
}