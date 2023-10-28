package com.captain.services.navsvc

data class NavMap (
    val landmarks : Map<String,Landmark>,
    val shape : String,
    val boundaries : MapBoundaries,
    val obstacles : Map<String,Obstacle>,
    val search : Map<String,Searchable>
)