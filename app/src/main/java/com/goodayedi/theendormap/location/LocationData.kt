package com.goodayedi.theendormap.location

import android.location.Location

data class LocationData(
    val location: Location? = null,
    val exception: Exception? = null
)