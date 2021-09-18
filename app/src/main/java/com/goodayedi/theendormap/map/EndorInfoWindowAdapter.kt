package com.goodayedi.theendormap.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.goodayedi.theendormap.R
import com.goodayedi.theendormap.poi.POI
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class EndorInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
    private val  contents = LayoutInflater.from(context).inflate(R.layout.info_window_endor, null)
    override fun getInfoWindow(marker: Marker): View? {
        val poi = marker.tag as POI
        with(contents) {
            val imageId = if(poi.imageId > 0) poi.imageId else R.drawable.marker_frodo
            findViewById<ImageView>(R.id.imageView).setImageResource(imageId)
            findViewById<TextView>(R.id.titleTextView).text = poi.title
            findViewById<TextView>(R.id.descriptionTextView).text = poi.description
        }
        return contents
    }

    override fun getInfoContents(p0: Marker): View? {
        return null
    }
}