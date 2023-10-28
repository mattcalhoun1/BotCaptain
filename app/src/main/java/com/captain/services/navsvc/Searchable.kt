package com.captain.services.navsvc

data class Searchable(
    val pattern : String,
    val model : String,
    val height : Float,
    val confidence : Float,
    val lidarVisible : Boolean
)
