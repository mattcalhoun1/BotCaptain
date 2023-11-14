package com.captain.services.navsvc

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NavSvc {
    @GET("shutdown/")
    suspend fun shutdownMobileNetwork () : Response<GenericResponse>

    @GET("vehicles/")
    suspend fun getVehicles () : Response<List<Vehicle>>

    @GET("recent_sessions/{vehicleId}/")
    suspend fun getRecentSessions (@Path("vehicleId") vehicleId :String) : Response<List<BotSession>>

    @GET("/position_log/{vehicleId}/{sessionId}/")
    suspend fun getPositionLog (@Path("vehicleId") vehicleId :String, @Path("sessionId") sessionId : String) : Response<List<PositionLogEntry>>

    @GET("/position_views/{vehicleId}/{sessionId}/")
    suspend fun getPositionViews (@Path("vehicleId") vehicleId :String, @Path("sessionId") sessionId : String) : Response<List<PositionView>>

    @GET("/position_view/{vehicleId}/{entryNum}/{cameraId}/")
    suspend fun getPositionImage (@Path("vehicleId") vehicleId :String, @Path("entryNum") entryNum : Long, @Path("cameraId") cameraId : String) : Response<PositionImage>

    @GET("/position_view/{vehicleId}/{entryNum}/{cameraId}/{cameraAngle}")
    suspend fun getPositionImage (@Path("vehicleId") vehicleId :String, @Path("entryNum") entryNum : Long, @Path("cameraId") cameraId : String, @Path("cameraAngle") cameraAngle : Float) : Response<PositionImage>

    @GET("/nav_map/{mapId}/")
    suspend fun getMap(@Path("mapId") mapId : String) : Response<NavMap>

    @GET("/nav_maps/")
    suspend fun getMaps() : Response<List<NavMapContainer>>

    @POST("/assignment_create/{vehicleId}/")
    suspend fun createAssignment(@Path("vehicleId") vehicleId : String, @Body assignment : Assignment) : Response<Assignment>

    @GET("/assignments/{vehicleId}/")
    suspend fun getAssignments(@Path("vehicleId") vehicleId : String) : Response<List<Assignment>>

    @POST("/assignment/{vehicleId}/{entryNum}/")
    suspend fun completeAssignment(@Path("vehicleId") vehicleId : String, @Path("entryNum") entryNum : Int, @Body assignment : AssignmentDetails) : Response<AssignmentDetails>

    @GET("/search_hits/{vehicleId}/{sessionId}/")
    suspend fun getSearchHits (@Path("vehicleId") vehicleId :String, @Path("sessionId") sessionId : String) : Response<List<SearchHit>>

    @GET("/search_hit/{objectType}/{mapId}/{entryNum}/")
    suspend fun getSearchHitImage (@Path("objectType") objectType :String, @Path("mapId") mapId : String, @Path("entryNum") entryNum : Int) : Response<SearchHitImage>

    @GET("/lidar_entries/{vehicleId}/{sessionId}/")
    suspend fun getLidarEntries (@Path("vehicleId") vehicleId :String, @Path("sessionId") sessionId : String) : Response<List<LidarLogEntry>>

    @GET("/lidar/{vehicleId}/{sessionId}/{entryNum}/")
    suspend fun getLidarMap (@Path("vehicleId") vehicleId :String, @Path("sessionId") sessionId : String, @Path("entryNum") entryNum : Long) : Response<LidarMap>

}
