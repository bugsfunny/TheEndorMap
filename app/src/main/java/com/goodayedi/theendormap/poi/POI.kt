package com.goodayedi.theendormap.poi

/**
 * iconId and iconColor are mutually exclusive.
 * if the iconId is defined, the iconColor should be left to the default value
 */
data class POI(
    val title: String,
    var latitude: Double,
    var longitude: Double,
    val imageId: Int = 0,
    val iconId: Int = 0,
    val iconColor: Int = 0,
    val description: String = "",
    val detailUrl:String = ""
)