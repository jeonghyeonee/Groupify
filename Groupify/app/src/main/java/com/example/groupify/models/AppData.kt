package com.example.groupify.models

import android.graphics.drawable.Drawable

data class AppData(
    val packageName: String, // 앱의 패키지 이름
    val appName: String,     // 앱의 이름
    val appIcon: Drawable    // 앱의 아이콘
)
