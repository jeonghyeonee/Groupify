package com.example.groupify.models

import java.io.Serializable

// 앱 이름, 패키지 이름, Dominant Color를 저장할 data class
data class AppInfo(
    val appName: String,
    val packageName: String,
    val dominantColor: String
) : Serializable
