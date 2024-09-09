package com.example.groupify

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.ScrollView
import com.google.firebase.storage.FirebaseStorage

class ClusteringActivity : AppCompatActivity() {

    private lateinit var appContainer: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var scrollViewContainer: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clustering)

        appContainer = findViewById(R.id.appContainer)
        loadingLayout = findViewById(R.id.loadingLayout)
        scrollViewContainer = findViewById(R.id.scrollViewContainer)

        // Firebase Storage에서 파일을 다운로드하고 파싱
        downloadAndParseFile()
    }

    private fun downloadAndParseFile() {
        val storageRef = FirebaseStorage.getInstance().reference.child("results/color_result.txt")
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val resultText = String(bytes)
            parseResultText(resultText)
        }.addOnFailureListener {
            Log.e("ClusteringActivity", "Failed to download file from Firebase", it)
        }
    }

    private fun parseResultText(resultText: String) {
        val lines = resultText.split("\n")
        val clusterMap = mutableMapOf<String, MutableList<Pair<String, String>>>()

        for (line in lines) {
            if (line.isNotEmpty()) {
                val parts = line.split(", ")
                val appName = parts[0].split(": ")[1]
                val color = parts[2].split(": ")[1]
                val cluster = parts[3].split(": ")[1]

                if (!clusterMap.containsKey(cluster)) {
                    clusterMap[cluster] = mutableListOf()
                }
                clusterMap[cluster]?.add(Pair(appName, color))
            }
        }

        displayClusteredApps(clusterMap)
    }

    private fun displayClusteredApps(clusterMap: Map<String, List<Pair<String, String>>>) {
        // 데이터가 준비되었으므로 로딩 화면을 숨기고 실제 데이터를 보여줍니다.
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

            for ((appName, color) in apps) {
                val appLayout = LinearLayout(this)
                appLayout.orientation = LinearLayout.HORIZONTAL

                val imageView = ImageView(this)

                try {
                    val packageManager: PackageManager = packageManager
                    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                    var appIconFound = false

                    for (appInfo in installedApps) {
                        val installedAppName = appInfo.loadLabel(packageManager).toString()
                        if (installedAppName.equals(appName, ignoreCase = true)) {
                            imageView.setImageDrawable(appInfo.loadIcon(packageManager))
                            appIconFound = true
                            break
                        }
                    }

                    if (!appIconFound) {
                        imageView.setImageResource(R.drawable.default_icon)
                    }
                } catch (e: Exception) {
                    Log.e("ClusteringActivity", "Error loading icon for app: $appName", e)
                    imageView.setImageResource(R.drawable.default_icon)
                }

                imageView.layoutParams = LinearLayout.LayoutParams(100, 100)

                val textView = TextView(this)
                textView.text = appName
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundColor(Color.parseColor(color))
                textView.setPadding(20, 0, 0, 0)

                appLayout.addView(imageView)
                appLayout.addView(textView)

                appContainer.addView(appLayout)
            }
        }
    }
}