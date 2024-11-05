package com.example.groupify.network

import com.example.groupify.ClusterRequest
import com.example.groupify.ClusterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FlaskApi {
    @Headers("Content-Type: application/json")
    @POST("/cluster")
    fun getClusteredApps(@Body requestBody: ClusterRequest): Call<ClusterResponse>
}