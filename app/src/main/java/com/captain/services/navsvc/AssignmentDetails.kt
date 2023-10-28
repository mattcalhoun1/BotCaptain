package com.captain.services.navsvc

data class AssignmentDetails (
    val mapId : String,
    val steps : List<AssignmentStep>,
    var entryNum : Int?,
    var complete : Boolean?,
    var vehicleId : String?
)