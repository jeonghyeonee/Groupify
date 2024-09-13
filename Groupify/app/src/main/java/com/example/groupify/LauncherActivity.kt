package com.example.groupify

import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlin.math.ceil

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // 배경화면 설정
        setupBackground()

        // 'clusteredApps' 데이터 수신
        val clusteredApps = intent.getSerializableExtra("clusteredApps") as? Map<String, List<Pair<String, String>>>
        if (clusteredApps == null) {
            Log.e("LauncherActivity", "clusteredApps is null")
        } else {
            Log.d("LauncherActivity", "Received clusteredApps with ${clusteredApps.size} clusters")
        }

        // 폴더를 GridLayout으로 한 화면에 표시
        clusteredApps?.let {
            displayFolders(it)
        }

        // 하단 Dock 설정
        clusteredApps?.let {
            setupDock(it)
        }
    }

    // 폴더를 한 화면에 모두 표시하는 함수
    private fun displayFolders(clusteredApps: Map<String, List<Pair<String, String>>>) {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout) // activity_launcher.xml에 정의된 GridLayout
        gridLayout.columnCount = 3 // 한 줄에 3개의 폴더를 배치

        // 각 클러스터에 대해 폴더 생성
        for ((cluster, apps) in clusteredApps) {
            val folderLayout = createFolderLayout(cluster, apps)
            gridLayout.addView(folderLayout)
        }
    }

    // 배경화면 설정 메서드
    private fun setupBackground() {
        val layout = findViewById<LinearLayout>(R.id.mainLayout)
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        layout.background = wallpaperDrawable
    }

    // 하단 Dock 설정 메서드 (기존 기능 유지)
    private fun setupDock(clusteredApps: Map<String, List<Pair<String, String>>>) {
        val dockLayout = findViewById<LinearLayout>(R.id.dockLayout)

        // 클러스터에서 임의로 몇 개의 앱을 가져와서 Dock에 배치
        val dockApps = clusteredApps.values.flatten().take(5) // 임의로 5개의 앱을 선택
        for ((packageName, _) in dockApps) {
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

    private fun createFolderLayout(cluster: String, appList: List<Pair<String, String>>): LinearLayout {
        val folderLayout = LinearLayout(this)
        folderLayout.orientation = LinearLayout.VERTICAL
        folderLayout.setPadding(8, 8, 8, 8)

        // 폴더 아이콘 생성
        val folderIcon = ImageView(this)
        folderIcon.setImageResource(R.drawable.baseline_folder_open_24) // 폴더 배경 이미지
        folderIcon.layoutParams = LinearLayout.LayoutParams(150, 150)

        // 미리보기로 보여줄 앱 아이콘 2~3개 겹치기
        val previewContainer = FrameLayout(this)
        previewContainer.layoutParams = FrameLayout.LayoutParams(150, 150)

        val maxPreviewApps = 3
        for (i in 0 until minOf(maxPreviewApps, appList.size)) {
            val appIcon = ImageView(this)
            val packageName = appList[i].first
            try {
                val icon = packageManager.getApplicationIcon(packageName)
                appIcon.setImageDrawable(icon)
                val size = 60 - (i * 10) // 겹치는 앱 아이콘 크기 줄이기
                val params = FrameLayout.LayoutParams(size, size)
                params.setMargins(i * 20, i * 20, 0, 0) // 각 아이콘 위치 겹치기
                appIcon.layoutParams = params
                previewContainer.addView(appIcon)
            } catch (e: PackageManager.NameNotFoundException) {
                appIcon.setImageResource(R.drawable.default_icon)
            }
        }

        folderLayout.addView(folderIcon)
        folderLayout.addView(previewContainer)

        // 폴더 클릭 시 앱 목록 표시
        folderLayout.setOnClickListener {
            showFolderDialog(appList)
        }

        return folderLayout
    }

    // LauncherActivity에 추가
    private fun showFolderDialog(appList: List<Pair<String, String>>) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Apps in this Folder")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        // 앱 목록을 동적으로 추가
        for ((packageName, _) in appList) {
            val appLayout = createAppLayout(packageName)
            layout.addView(appLayout)
        }

        dialog.setView(layout)
        dialog.setNegativeButton("Close", null)
        dialog.show()
    }

    // LauncherActivity에 추가
    private fun createAppLayout(packageName: String): LinearLayout {
        val appLayout = LinearLayout(this)
        appLayout.orientation = LinearLayout.HORIZONTAL
        appLayout.setPadding(8, 8, 8, 8)

        val imageView = ImageView(this)
        try {
            // 패키지 이름으로 앱 아이콘 가져오기
            val appIcon = packageManager.getApplicationIcon(packageName)
            imageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            // 앱이 없을 경우 기본 아이콘 표시
            imageView.setImageResource(R.drawable.default_icon)
        }

        imageView.layoutParams = LinearLayout.LayoutParams(100, 100)

        val textView = TextView(this)
        try {
            // 패키지 이름으로 앱 이름 가져오기
            val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
            textView.text = appName
        } catch (e: PackageManager.NameNotFoundException) {
            // 앱 이름이 없으면 패키지 이름으로 대체
            textView.text = packageName
        }
        textView.setPadding(16, 0, 0, 0)

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱 클릭 시 실행
        appLayout.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        return appLayout
    }



}
