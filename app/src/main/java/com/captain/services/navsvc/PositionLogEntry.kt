package com.captain.services.navsvc

data class PositionLogEntry (
    val entryNum : Long,
    val occurred : String,
    val vehicleId: String,
    val sessionId: String,
    val created: String,
    val positionX: Float,
    val positionY: Float,
    val heading: Float,
    val navmapId: String
)