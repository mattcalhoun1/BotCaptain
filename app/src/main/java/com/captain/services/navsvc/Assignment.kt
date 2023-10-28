package com.captain.services.navsvc

data class Assignment (
    val vehicleId : String,
    val assignment : AssignmentDetails,
    var entryNum : Int?
)