package com.captain.services.navsvc

interface BotImage {
    val entryNum : Long
    val imageFormat : String
    val encodedImage : String
    val cameraId : String
    val cameraAngle : Long
}