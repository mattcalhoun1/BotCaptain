package com.captain.services.navsvc

data class SearchHitImage (
    val objectType : String,
    override val entryNum : Long,
    val mapId: String,
    override val imageFormat: String,
    override val encodedImage: String,
    override val cameraId : String = "na",
    override val cameraAngle : Long
) : BotImage
