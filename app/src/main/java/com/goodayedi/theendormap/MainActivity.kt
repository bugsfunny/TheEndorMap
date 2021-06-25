package com.goodayedi.theendormap

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import timber.log.Timber
import java.lang.Exception

private const val REQUEST_LOCATION_PERMISSION_START_UPDATE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var locationLiveData: LocationLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationLiveData = LocationLiveData(this)
        locationLiveData.observe(this){
            handleLocationData(it!!)
        }


    }

    private fun handleLocationData(locationData: LocationData) {
        if (handleLocationException(locationData.exception)){
            return
        }
        Timber.i("Last location from liveData ${locationData.location}")
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
}