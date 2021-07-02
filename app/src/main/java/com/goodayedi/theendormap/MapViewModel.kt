package com.goodayedi.theendormap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class MapUIState {
    object Loading: MapUIState()
    data class Error(val message: String): MapUIState()
    data class POIReady(
        val userPOI: POI? = null,
        val POIList: List<POI>? = null
    ) :  MapUIState()
}

class MapViewModel: ViewModel() {
    private val uiState = MutableLiveData<MapUIState>()
    fun getUiState(): LiveData<MapUIState> = uiState

    fun loadPOIList(latitude: Double, longitude: Double) {
        if(!(latitude in -90.0..90.0 && longitude in -180.0..180.0)){
            uiState.value = MapUIState.Error("Invalid coordinates: lat=$latitude, long=$longitude")
            return
        }
        uiState.value = MapUIState.Loading
        uiState.value = MapUIState.POIReady(
            userPOI = generateUserPOI(latitude, longitude),
            POIList = generatePOIList(latitude, longitude)
        )
    }
}