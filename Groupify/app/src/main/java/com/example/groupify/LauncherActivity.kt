package com.example.groupify

import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlin.math.ceil

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // 배경화면 설정
        setupBackground()

        // 클러스터링된 앱 목록을 Intent로 전달받음
        val clusteredApps = intent.getSerializableExtra("clusteredApps") as? ArrayList<Pair<String, String>>

        // ViewPager 설정
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        clusteredApps?.let {
            val adapter = ViewPagerAdapter(supportFragmentManager, getAppPages(it))
            viewPager.adapter = adapter
        }

        // 하단 Dock 설정
        setupDock()
    }

    // 앱을 페이지로 나누는 함수
    private fun getAppPages(clusteredApps: ArrayList<Pair<String, String>>): List<List<Pair<String, String>>> {
        // 한 페이지당 20개씩 앱을 나눔 (4x5)
        val pageSize = 20
        return clusteredApps.chunked(pageSize)
    }

    // 배경화면 설정 메서드
    private fun setupBackground() {
        val layout = findViewById<LinearLayout>(R.id.mainLayout)
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        layout.background = wallpaperDrawable
    }

    // 하단 Dock 설정 메서드
    private fun setupDock() {
        val dockLayout = findViewById<LinearLayout>(R.id.dockLayout)
        val packageNames = listOf("com.example.app1", "com.example.app2") // 예시 앱 패키지들

        for (packageName in packageNames) {
            try {
                val appIcon = packageManager.getApplicationIcon(packageName)
                val imageView = ImageView(this)
                imageView.setImageDrawable(appIcon)
                imageView.layoutParams = LinearLayout.LayoutParams(150, 150)
                imageView.setOnClickListener {
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    }
                }
                dockLayout.addView(imageView)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    // 뒤로가기 버튼 무시
    override fun onBackPressed() {
        // 아무것도 하지 않음으로써 앱이 종료되지 않게 함
    }
}
