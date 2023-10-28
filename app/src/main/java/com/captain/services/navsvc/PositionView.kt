package com.captain.services.navsvc

data class PositionView (
    val vehicleId : String,
    val entryNum : Long,
    val sessionId: String,
    val cameraId: String,
    val cameraAngle: Float,
    val imageFormat: String,
    val encodedImage: String
)
