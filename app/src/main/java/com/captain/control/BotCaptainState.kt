package com.captain.control

import com.captain.services.navsvc.AssignmentDetails
import com.captain.services.navsvc.BotImage
import com.captain.services.navsvc.BotSession
import com.captain.services.navsvc.LidarLogEntry
import com.captain.services.navsvc.LidarMap
import com.captain.services.navsvc.NavMap
import com.captain.services.navsvc.PositionLogEntry
import com.captain.services.navsvc.PositionView
import com.captain.services.navsvc.SearchHit
import com.captain.services.navsvc.Vehicle

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
    var allMaps : HashMap<String, NavMap>,
    var assignmentMap : String,
    var openAssignments : ArrayList<AssignmentDetails>,
    var lidarLog: ArrayList<LidarLogEntry>,
    var lidarMaps: ArrayList<LidarMap>
)