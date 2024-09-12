package com.example.groupify

import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // 배경화면 설정
        setupBackground()

        // 하단 Dock 설정
        setupDock()

        // 설치된 모든 앱 목록 가져오기
        gridLayout = findViewById(R.id.gridLayout)
        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        // 설치된 앱을 그리드에 추가
        for (appInfo in installedApps) {
            val appName = appInfo.loadLabel(packageManager).toString()
            val packageName = appInfo.packageName

            // 앱 아이콘과 이름을 그리드에 추가
            val appLayout = createAppLayout(appName, packageName)
            gridLayout.addView(appLayout)
        }
    }

    // 앱의 아이콘과 이름을 담은 레이아웃을 생성하는 함수
    private fun createAppLayout(appName: String, packageName: String): LinearLayout {
        val appLayout = LinearLayout(this)
        appLayout.orientation = LinearLayout.VERTICAL
        appLayout.setPadding(8, 8, 8, 8)

        val imageView = ImageView(this)
        try {
            val appIcon = packageManager.getApplicationIcon(packageName)
            imageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            imageView.setImageResource(R.drawable.default_icon)
        }

        imageView.layoutParams = LinearLayout.LayoutParams(150, 150)

        val textView = TextView(this)
        textView.text = appName
        textView.setTextColor(Color.BLACK)

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱 아이콘 클릭 시 앱 실행
        imageView.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Log.e("LauncherActivity", "App not found: $packageName")
            }
        }

        return appLayout
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
        // 자주 사용하는 앱을 Dock에 추가
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
