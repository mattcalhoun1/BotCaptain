package com.captain.services.navsvc

data class SearchHit(
    val entryNum : Int,
    val sessionId: String,
    val vehicleId : String,
    val objectType : String,
    val mapId : String,
    val occurred : String,
    val estVisualDist : Float,
    val estLidarDist : Float,
    val vehicleRelativeHeading : Float,
    val estX : Float,
    val estY : Float,
    val vehicleX : Float,
    val vehicleY : Float,
    val vehicleHeading : Float,
    val confidence : Float,
    val cameraId: String,
    val cameraAngle: Float,
    val imageFormat: String
    // val encodedImage: String
)
