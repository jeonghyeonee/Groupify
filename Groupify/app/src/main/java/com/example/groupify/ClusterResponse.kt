package com.example.groupify

// 서버에서 받을 응답 데이터 클래스
data class ClusterResponse(
    val clusters: Map<String, ClusterData>
)