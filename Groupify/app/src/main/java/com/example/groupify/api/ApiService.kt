package com.example.groupify.api

import com.example.groupify.models.YourDataResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("http://ec2-13-125-56-70.ap-northeast-2.compute.amazonaws.com:8080")
    fun getYourData(
        @Query("param1") param1: String
    ): Call<YourDataResponse> // YourDataResponse는 서버에서 반환하는 데이터 클래스입니다.
}