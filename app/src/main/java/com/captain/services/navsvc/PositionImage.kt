package com.captain.services.navsvc

data class PositionImage (
    override val imageFormat : String,
    override val entryNum : Long,
    val vehicleId : String,
    override val cameraId : String,
    override val encodedImage : String,
    override val cameraAngle : Long) : BotImage
