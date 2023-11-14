package com.captain.services.navsvc

data class LidarMap(
    val occurred : String,
    val vehicleId: String,
    val sessionId: String,
    val lidarData: Map<String,Float>
)