package com.goodayedi.theendormap

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import timber.log.Timber
import java.lang.Exception

data class LocationData(
    val location: Location? = null,
    val exception: Exception? = null
)

class LocationLiveData(context: Context): LiveData<LocationData>() {
    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                Timber.i("location update: $location")
                value = LocationData(location)
            }
        }
    }
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var firstSubscriber = true

    override fun onActive() {
        super.onActive()
        if (firstSubscriber){
            requestLastLocation()
            createLocationRequest()
            firstSubscriber = false
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        firstSubscriber = true
    }

    fun createLocationRequest() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(appContext)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Timber.i("Location settings: ok")
            requestLocation()
        }
        task.addOnFailureListener {
            Timber.i("Location settings: $it")
            if (it is ResolvableApiException){
                value = LocationData(exception = it)

            }
        }
    }

    private fun requestLocation() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException){
            value = LocationData(exception = e)
        }
    }

    private fun requestLastLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                value = LocationData(location = location)
            }
            fusedLocationClient.lastLocation.addOnFailureListener { exception ->
                value = LocationData(exception = exception)
            }
        } catch (e: SecurityException){
            value = LocationData(exception = e)
        }
    }
}
