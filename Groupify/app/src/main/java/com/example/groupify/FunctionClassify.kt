package com.example.groupify

import android.app.WallpaperManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

class FunctionClassify : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classify_function)

        // 배경화면을 가져와서 적용
        val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable: Drawable? = wallpaperManager.drawable

        // 배경화면을 ImageView에 적용 (layout에서 ImageView를 사용하여 배경 설정)
        val backgroundImageView: ImageView = findViewById(R.id.backgroundImageView)
        backgroundImageView.setImageDrawable(wallpaperDrawable)

        // 나머지 앱 아이콘 및 카테고리 표시 로직
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // CSV 파일에서 카테고리 데이터를 로드
        val appsData = loadCSVFromAsset("apps_category_mapping.csv")

        // 설치된 앱 필터링
        val installedApps = getVisibleApps()

        // 설치된 앱들을 슈퍼 카테고리로 분류
        val categorizedApps = classifyAppsBySuperCategory(installedApps, appsData)

        // RecyclerView 어댑터 설정
        val adapter = CategoryAdapter(categorizedApps, packageManager)
        recyclerView.adapter = adapter
    }


    // CSV 파일을 읽어오는 함수
    private fun loadCSVFromAsset(filename: String): List<List<String>> {
        val appsData = mutableListOf<List<String>>()
        try {
            val inputStream = assets.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLine() // 첫 번째 줄은 헤더이므로 건너뜁니다.

            var line: String? = reader.readLine()
            while (line != null) {
                val columns = line.split(",")
                appsData.add(columns)
                line = reader.readLine()
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return appsData
    }

    // 설치된 앱 중 실제로 런처에 표시되는 앱 필터링
    private fun getVisibleApps(): List<ApplicationInfo> {
        val packageManager = packageManager
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return allApps.filter { appInfo ->
            val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            launchIntent != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }
    }

    // 설치된 앱을 슈퍼 카테고리로 분류하는 함수
    private fun classifyAppsBySuperCategory(installedApps: List<ApplicationInfo>, appsData: List<List<String>>): Map<String, List<ApplicationInfo>> {
        val categorizedApps = mutableMapOf<String, MutableList<ApplicationInfo>>()

        for (appInfo in installedApps) {
            val appName = packageManager.getApplicationLabel(appInfo).toString()

            for (appData in appsData) {
                val name = appData[0]
                val superCategory = appData[2]

                if (appName == name) {
                    categorizedApps.computeIfAbsent(superCategory) { mutableListOf() }.add(appInfo)
                }
            }
        }
        return categorizedApps
    }
}
