package com.captain.services.navsvc

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.text.format.Formatter
import com.captain.MainActivity

interface VehicleSelectionListener {
    fun vehicleSelected (vehicleId : String)
}


class BotCaptainEvents constructor (state : BotCaptainState, mainActivity: MainActivity){
    private var state : BotCaptainState = state
    private var mainActivity : MainActivity = mainActivity
    private val vehicleSelectionListeners = ArrayList<MutableState<String>>()
    private val sessionSelectionListeners = ArrayList<MutableState<String>>()
    private val positionLogListeners = ArrayList<MutableState<String>>()
    private val positionImageListeners = ArrayList<MutableState<String>>()
    private val navMapListeners = ArrayList<MutableState<String>>()
    private val assignmentMapListeners = ArrayList<MutableState<String>>()
    private val assignmentRefreshListeners = ArrayList<MutableState<String>>()

    private lateinit var navSvcApi : NavSvc

    // home wifi: "http://10.0.0.198:8000/"
    // mobile: "http://192.168.0.100:8000/"
    private val SERVICE_URLS = arrayListOf<String>(
        "http://192.168.0.100:8000/", // mobile wifi
        "http://10.0.0.199:8000/", // home wifi
        //"http://10.0.0.122:8000/", // localhost when running on desktop
    )
    private var preloadPositionImages = true

    fun refreshSession () {
        getPositionLog()
        getPositionViews()
    }

    fun setPreloadPositionImages (preload: Boolean) {
        preloadPositionImages = preload
    }

    fun isVehicleSelected (vehicleId: String) : Boolean {
        return state.selectedVehicle.equals(vehicleId)
    }

    fun setVehicleSelectionListener (vehicleSelectionListener: MutableState<String>) {
        // this is a hack. events need to be handled differently
        if (vehicleSelectionListeners.size > 8) {
            vehicleSelectionListeners.removeAt(0)
        }
        vehicleSelectionListeners.add(vehicleSelectionListener)
    }

    fun vehicleSelected (vehicleId: String) {
        Log.i("Events","vehicle selected: ${vehicleId} (Previous: ${state.selectedVehicle})")
        state.selectedVehicle = vehicleId
        for (l in vehicleSelectionListeners) {
            Log.i("Events","Triggering a listner from events")

            l.value = vehicleId
        }

        // Update sessions
        getRecentSessions()

        // Load maps if not alredy loaded
        loadAllMaps()

        // load any open assignments
        getOpenAssignments()
    }

    fun isSessionSelected (sessionId: String) : Boolean {
        return state.selectedSession.equals(sessionId)
    }

    fun setSessionSelectionListener (sessionSelectionListener: MutableState<String>) {
        // this is a hack. events need to be handled differently
        if (sessionSelectionListeners.size > 5) {
            sessionSelectionListeners.removeAt(0)
        }
        sessionSelectionListeners.add(sessionSelectionListener)
    }

    fun sessionSelected (sessionId: String) {
        Log.i("Events","session selected: ${sessionId} (Previous: ${state.selectedSession})")
        state.selectedSession = sessionId
        for (l in sessionSelectionListeners) {
            Log.i("Events","Triggering a listner from events")

            l.value = sessionId
        }

        // trigger position log load
        getPositionLog()
        getPositionViews()
    }

    fun setPositionLogListener (vehicleSelectionListener: MutableState<String>) {
        if (positionLogListeners.size > 5) {
            positionLogListeners.removeAt(0)
        }

        positionLogListeners.add(vehicleSelectionListener)
    }

    fun positionLogLoaded () {
        for (l in positionLogListeners) {
            l.value = state.newestLogEntry
        }
    }

    fun setNavMapListener (navMapListener: MutableState<String>) {
        if (navMapListeners.size > 5) {
            navMapListeners.removeAt(0)
        }

        navMapListeners.add(navMapListener)
    }

    fun navMapLoaded () {
        for (l in navMapListeners) {
            l.value = state.newestLogEntry
        }
    }

    fun setAssignmentRefreshListener (assignmentRefreshListener: MutableState<String>) {
        // this is a hack. events need to be handled differently
        if (assignmentRefreshListeners.size > 5) {
            assignmentRefreshListeners.removeAt(0)
        }
        assignmentRefreshListeners.add(assignmentRefreshListener)
    }

    fun assignmentRefresh () {
        for (r in assignmentRefreshListeners) {
            r.value = "${state.openAssignments.size}"
        }
    }

    fun setAssignmentMapListener (assignmentMapListener: MutableState<String>) {
        if (assignmentMapListeners.size > 5) {
            assignmentMapListeners.removeAt(0)
        }

        assignmentMapListeners.add(assignmentMapListener)
    }

    fun assignmentMapSelected (mapId : String) {
        state.assignmentMap = mapId
        for (l in assignmentMapListeners) {
            l.value = state.assignmentMap
        }
    }

    fun isAssignmentMapSelected (mapId: String) : Boolean {
        return state.assignmentMap.equals(mapId)
    }

    fun setPositionImageListener (positionImageListener: MutableState<String>) {
        if (positionImageListeners.size > 5) {
            positionImageListeners.removeAt(0)
        }
        positionImageListeners.add(positionImageListener)
    }

    fun positionImageLoaded () {
        for (l in positionImageListeners) {
            l.value = state.newestLogEntry
        }
    }

    fun getAllVehicles () {
        mainActivity.showProgressBar()
        val api = getNavSvcApi()

        GlobalScope.launch(Dispatchers.IO) {
            mainActivity.showProgressBar()
            vehicleSelected("")

            val response = api.getVehicles()
            if (response.isSuccessful()) {
                // rebuild the vehicle list
                state.vehicles.clear()
                for (v in response.body()!!) {
                    state.vehicles.add(v)
                    //Log.i(TAG,"getAllVehicles: ${v.vehicleId}")
                }

                mainActivity.hideProgressBar()
                vehicleSelected("")
            }
        }
    }

    private fun getTimestampForEvent(eventNum: Long) : String {
        // clone it so we dont hit thraeading issue?
        for (l in state.positionLog) {
            if (l.entryNum == eventNum) {
                return l.occurred
            }
        }

        return "unknown"
    }

    private fun getRecentSessions () {
        state.botSessions.clear()
        if (!state.selectedVehicle.equals("")) {
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getRecentSessions(state.selectedVehicle)
                if (response.isSuccessful()) {
                    // rebuild the vehicle list
                    state.botSessions.clear()
                    for (s in response.body()!!) {
                        state.botSessions.add(s)
                        //Log.i(TAG,"getAllVehicles: ${v.vehicleId}")
                    }

                    // Select the first session, or none, and trigger the session ui update
                    sessionSelected(if (state.botSessions.size > 0) state.botSessions.get(0).sessionId else "")

                }
            }
        }
    }

    private fun getNavMap () {
        if (state.positionLog.size > 0) {
            // grab the first position's map
            val map_id = state.positionLog.get(0).navmapId
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getMap(map_id)
                if (response.isSuccessful()) {
                    state.navMap = response.body()!!

                    // trigger a ui update
                    navMapLoaded()
                }
            }

        }
    }
    private fun loadAllMaps () {
        // only if not already loaded
        if (state.allMaps.size == 0) {
            // grab the first position's map
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getMaps()
                var mapCount = 0
                if (response.isSuccessful()) {
                    val mapContainers = response.body()!!
                    for (c in mapContainers) {
                        state.allMaps[c.mapId] = c.content

                        // first one is selected by default
                        if (mapCount == 0) {
                            state.assignmentMap = c.mapId
                        }
                        mapCount += 1
                    }
                }
            }

        }
    }

    private fun getPositionLog () {
        state.positionLog.clear()
        state.newestLogEntry = ""
        if (!state.selectedVehicle.equals("") && !state.selectedSession.equals("")) {
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getPositionLog(state.selectedVehicle, state.selectedSession)
                if (response.isSuccessful()) {
                    // rebuild the vehicle list
                    for (le in response.body()!!) {
                        state.positionLog.add(le)
                        //Log.i(TAG,"getAllVehicles: ${v.vehicleId}")
                    }

                    if (state.positionLog.size > 0) {
                        state.newestLogEntry = state.positionLog.get(0).occurred
                    }

                    // load search hits from session
                    getSearchHits()

                    // load appropriate map
                    getNavMap()

                    // trigger a ui update
                    positionLogLoaded()
                }
            }
        }
    }

    private fun getSearchHits () {
        state.searchHits.clear()
        if (!state.selectedVehicle.equals("") && !state.selectedSession.equals("")) {
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getSearchHits(state.selectedVehicle, state.selectedSession)
                if (response.isSuccessful()) {
                    // rebuild the vehicle list
                    for (sh in response.body()!!) {
                        state.searchHits.add(sh)
                        //Log.i(TAG,"getAllVehicles: ${v.vehicleId}")
                    }
                }
            }
        }
    }

    private fun getPositionViews () {
        mainActivity.showProgressBar()

        state.positionViews.clear()
        state.cameras.clear()
        if (!state.selectedVehicle.equals("") && !state.selectedSession.equals("")) {

            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getPositionViews(state.selectedVehicle, state.selectedSession)
                if (response.isSuccessful()) {
                    // rebuild the vehicle list
                    for (v in response.body()!!) {
                        state.positionViews.add(v)

                        if(!state.cameras.contains(v.cameraId)) {
                            state.cameras.add(v.cameraId)
                        }
                        Log.i("api", "${v}")

                    }

                    state.cameras.sort()

                    // If preload is turned on, pull latest image from each camera
                    state.positionImages.clear()
                    if (preloadPositionImages) {
                        //for (c in state.cameras) {
                           for (v in state.positionViews.reversed()) {
                               //if (v.cameraId.equals(c)) {
                                   // Grab this image and move on to the next camera
                                   val imageResp = api.getPositionImage(v.vehicleId, v.entryNum, v.cameraId, v.cameraAngle)
                                   if (imageResp.isSuccessful()) {
                                       // rebuild the vehicle list
                                       if (imageResp.body() != null) {
                                           state.newestLogEntry = getTimestampForEvent(v.entryNum)
                                           state.positionImages.add(imageResp.body()!!)
                                           Log.i("Position Image", "Found image: Entry: ${v.entryNum}, Camera: ${v.cameraId}, Angle: ${v.cameraAngle}")
                                       }
                                   }
                                   //break // move on to the next camera
                               //}
                           }
                        //}

                        // Pull search hits as well
                        val shResp = api.getSearchHits(state.selectedVehicle, state.selectedSession)
                        if (shResp.isSuccessful()) {
                            val hits = ArrayList<SearchHit>()
                            for (sh in shResp.body()!!) {
                                hits.add(sh)
                            }

                            // newest is at the end
                            hits.reverse()

                            // Display up to the last 2 search hits
                            var currHit = 0
                            while (currHit < 2 && currHit < hits.size) {
                                val h = hits.get(currHit)
                                // Retrieve the image for this hit
                                val imgResp = api.getSearchHitImage(h.objectType, h.mapId, h.entryNum)
                                if (imgResp.isSuccessful()) {
                                    state.positionImages.add(imgResp.body()!!)
                                }

                                currHit++
                            }
                        }
                    }

                    mainActivity.hideProgressBar()

                    // trigger a ui update
                    positionImageLoaded()
                }
            }
        }
    }

    fun selectMap (mapId : String) {
        state.assignmentMap = mapId
    }

    fun sendAssignmentGo(x: Float, y: Float) {
        // send an assignment to go to the selected vehcile id
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "GO", params = mapOf("X" to "${x}", "Y" to "${y}")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")

    }

    fun sendAssignmentFace(x: Float, y: Float) {
        // send an assignment to go to the selected vehcile id
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "FACE", params = mapOf("X" to "${x}", "Y" to "${y}")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")

    }

    fun sendAssignmentSearch(objectType : String) {
        // send an assignment to search to the selected vehicle id
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "SEARCH", params = mapOf("object" to objectType)))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")

    }

    fun sendAssignmentGetPosition () {
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "POSITION", params = mapOf("type" to "basic")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")
    }

    fun sendAssignmentLogLidar () {
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "LIDAR", params = mapOf("type" to "basic")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")
    }

    fun sendAssignmentBeginAutonomous() {
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "AUTONOMOUS", params = mapOf("type" to "basic")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")
    }

    fun sendAssignmentBeginControlled() {
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "CONTROLLED", params = mapOf("type" to "basic")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")
    }

    fun sendAssignmentShutdown () {
        val vehicleId = state.selectedVehicle
        val steps = ArrayList<AssignmentStep>()
        steps.add(AssignmentStep(command = "SHUTDOWN", params = mapOf("type" to "basic")))
        val assgn = Assignment (vehicleId=vehicleId, assignment = AssignmentDetails(mapId=state.assignmentMap,steps=steps,complete=null, entryNum = null, vehicleId = vehicleId), entryNum = null)

        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            val resp = api.createAssignment(vehicleId = vehicleId, assignment = assgn)
            getOpenAssignments() // reload assignments
        }
        Log.i("assign", "Sending assignment: ${assgn}")
    }

    fun clearAssignments () {
        val vehicleId = state.selectedVehicle
        val api = getNavSvcApi()
        GlobalScope.launch(Dispatchers.IO) {
            for (a in state.openAssignments) {
                Log.i("clearing","${a}")
                a.complete = true
                a.vehicleId = vehicleId
                val resp = api.completeAssignment(vehicleId = vehicleId, entryNum = a.entryNum!!, assignment = a)
            }
            getOpenAssignments() // reload assignments, should be empty now
        }
    }

    fun getOpenAssignments () {
        state.openAssignments.clear()
        state.newestLogEntry = ""
        if (!state.selectedVehicle.equals("")) {
            val api = getNavSvcApi()

            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getAssignments(state.selectedVehicle)
                if (response.isSuccessful()) {
                    // rebuild the vehicle list
                    Log.i("json", response.raw().body()!!.toString())
                    for (assgn in response.body()!!) {
                        // Copy the assignment details down into the child objects
                        // so they are availble later for udpates
                        assgn.assignment.entryNum = assgn.entryNum

                        state.openAssignments.add(assgn.assignment)

                        Log.i("assignment","${assgn}")
                    }

                    assignmentRefresh()
                }
            }
        }

    }

    fun shutdownMobileWifi () {
        Log.i("shutdown", "Shutting down mobile wifi")
        val api = getNavSvcApi()

        GlobalScope.launch(Dispatchers.IO) {
            val response = api.shutdownMobileNetwork()
            if (response.isSuccessful()) {
                // rebuild the vehicle list
                Log.i("json", response.raw().body()!!.toString())
            }
        }

    }

    private fun getNavSvcApi () : NavSvc {
        if (!::navSvcApi.isInitialized) {
            // Select the appropriate IP, based on which network connected to
            val wifiManager = mainActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress: String = "http://" + Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)

            // Select the closest matching ip
            val matches = arrayListOf<Int>()
            for (charCount in ipAddress.indices) {
                for (ipTrial in SERVICE_URLS.indices) {
                    if (charCount == 0) {
                        matches.add(0)
                    }

                    val strIpTrial = SERVICE_URLS.get(ipTrial)
                    if (charCount < strIpTrial.length) {
                        if (ipAddress[charCount] == strIpTrial[charCount]) {
                            matches[ipTrial] = matches[ipTrial] + 1
                        }
                    }
                }
            }

            // select the highest matching ip
            val finalIp = SERVICE_URLS[matches.indexOf(matches.max())]

            val gson =
                GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
            val api = Retrofit.Builder()
                .baseUrl(finalIp)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(NavSvc::class.java)

            navSvcApi = api
        }
        return navSvcApi
    }
}