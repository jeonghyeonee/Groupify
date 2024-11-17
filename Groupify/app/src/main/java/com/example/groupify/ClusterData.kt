package com.example.groupify

import com.example.groupify.models.AppData

data class ClusterData(
    val category_name: String,
    val apps: List<AppData>
)