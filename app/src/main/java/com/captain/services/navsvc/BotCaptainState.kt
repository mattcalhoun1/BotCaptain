package com.captain.services.navsvc

data class BotCaptainState (
    var selectedVehicle: String,
    var selectedSession: String,
    var botSessions: ArrayList<BotSession>,
    var vehicles: ArrayList<Vehicle>,
    var positionLog: ArrayList<PositionLogEntry>,
    var newestLogEntry: String,
    var searchHits: ArrayList<SearchHit>,
    var cameras: ArrayList<String>,
    var positionViews: ArrayList<PositionView>,
    var positionImages: ArrayList<BotImage>,
    var newestPositionImage: String,
    var navMap : NavMap,
    var allMaps : HashMap<String,NavMap>,
    var assignmentMap : String,
    var openAssignments : ArrayList<AssignmentDetails>
)