package com.example.groupify

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ClusteringActivity : AppCompatActivity() {

    private lateinit var appContainer: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var scrollViewContainer: ScrollView
    private lateinit var confirmButton: Button

    // 클러스터 데이터를 저장할 변수
    private val clusterMap = mutableMapOf<String, MutableList<Pair<String, String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clustering)

        appContainer = findViewById(R.id.appContainer)
        loadingLayout = findViewById(R.id.loadingLayout)
        scrollViewContainer = findViewById(R.id.scrollViewContainer)
        confirmButton = findViewById(R.id.confirmButton)

        // Firebase Storage에서 파일 다운로드 후 파싱
        downloadAndParseFile()

        // '확인' 버튼을 누르면 LauncherActivity 호출
        confirmButton.setOnClickListener {
            Log.d("ClusteringActivity", "Confirm button clicked")
            val intent = Intent(this, LauncherActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun downloadAndParseFile() {
        val storageRef = FirebaseStorage.getInstance().reference.child("results/logcat_SUA2_apps_ee21702d85b4e078.txt")

        // 디렉토리 경로를 설정합니다.
        val recentsDir = File(getExternalFilesDir(null), "Recents")

        // 디렉토리가 없으면 생성
        if (!recentsDir.exists()) {
            if (recentsDir.mkdirs()) {
                Log.d("ClusteringActivity", "Recents directory created successfully.")
            } else {
                Log.e("ClusteringActivity", "Failed to create recents directory.")
            }
        } else {
            Log.d("ClusteringActivity", "Recents directory already exists.")
        }

        // Firebase Storage에서 파일 다운로드
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val resultText = String(bytes)

            // 다운로드된 데이터를 파일로 저장
            val file = File(recentsDir, "logcat_SUA2_apps.txt")
            file.writeText(resultText)

            // 파일이 성공적으로 저장되었음을 로그로 확인
            Log.d("ClusteringActivity", "File saved at: ${file.absolutePath}")

            // 파일 파싱 및 처리
            parseResultText(resultText)
        }.addOnFailureListener {
            Log.e("ClusteringActivity", "Failed to download file from Firebase", it)
        }
    }

    private fun parseResultText(resultText: String) {
        val lines = resultText.split("\n")

        for (line in lines) {
            if (line.isNotEmpty()) {
                val parts = line.split(", ")
                val appName = parts[0].split(": ")[1]  // 앱 이름
                val packageName = parts[1].split(": ")[1]  // 패키지 이름
                val cluster = parts[2].split(": ")[1]  // 클러스터 번호

                // 클러스터 맵에 해당 클러스터가 없으면 추가
                if (!clusterMap.containsKey(cluster)) {
                    clusterMap[cluster] = mutableListOf()
                }
                clusterMap[cluster]?.add(Pair(appName, packageName))
            }
        }

        // UI에 클러스터링된 앱 표시
        displayClusteredApps(clusterMap)
    }

    private fun displayClusteredApps(clusterMap: Map<String, List<Pair<String, String>>>) {
        loadingLayout.visibility = LinearLayout.GONE
        scrollViewContainer.visibility = ScrollView.VISIBLE

        for ((cluster, apps) in clusterMap) {
            val clusterHeader = TextView(this)
            clusterHeader.text = "Cluster $cluster"
            clusterHeader.setTextColor(Color.BLACK)
            clusterHeader.setBackgroundColor(Color.LTGRAY)
            clusterHeader.textSize = 18f
            clusterHeader.setPadding(0, 20, 0, 10)
            appContainer.addView(clusterHeader)

            val appGridLayout = GridLayout(this)
            appGridLayout.columnCount = 3
            appContainer.addView(appGridLayout)

            for ((appName, packageName) in apps) {
                val appLayout = createAppLayout(appName, packageName)
                appGridLayout.addView(appLayout)
            }
        }
    }

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

        return appLayout
    }
}