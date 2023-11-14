package com.captain.services.navsvc

data class LidarLogEntry(
    val entryNum : Long,
    val occurred : String,
    val vehicleId: String,
    val sessionId: String
)