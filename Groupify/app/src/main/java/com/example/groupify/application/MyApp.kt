package com.example.groupify.application

import android.app.Application
import com.example.groupify.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApp : Application() {
    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("http://13.125.56.70:8080/")  // EC2 서버 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}