package com.example.groupify.models

import android.graphics.drawable.Drawable





// 폴더 정보를 저장하는 데이터 클래스
data class FolderData(
    val folderName: String,              // 폴더 이름
    val appList: MutableList<AppData>     // 폴더에 포함된 앱 리스트
)
