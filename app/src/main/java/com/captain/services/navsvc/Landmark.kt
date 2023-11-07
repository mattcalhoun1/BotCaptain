package com.captain.services.navsvc

data class Landmark (
    val pattern : String,
    val type : String,
    val model : String,
    val x : Float,
    val y : Float,
    val height : Float,
    val altitude : Float,
    val confidence : Float,
    val priority : Int,
    val lidarVisible: Boolean
)