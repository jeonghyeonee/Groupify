package com.example.groupify

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var homeContainer: GridLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d("sua", "HomeActivity 실행됨")

        homeContainer = findViewById(R.id.homeContainer)

        // 클러스터링된 앱 리스트를 불러와 폴더로 배치
        loadClusteredApps()
    }


    private fun loadClusteredApps() {
        val sharedPreferences = getSharedPreferences("launcher_preferences", MODE_PRIVATE)
        val allClusters = sharedPreferences.all

        // 저장된 모든 클러스터 데이터를 가져와 폴더로 추가
        for ((clusterKey, appListString) in allClusters) {
            val appList = appListString.toString().split(",")
            createFolder(clusterKey.replace("cluster_", "Cluster "), appList)
        }
    }

    private fun createFolder(folderName: String, apps: List<String>) {
        val folderLayout = LinearLayout(this)
        folderLayout.orientation = LinearLayout.VERTICAL

        val folderIcon = ImageView(this)
        folderIcon.setImageResource(R.drawable.folder_icon) // 폴더 아이콘

        val folderLabel = TextView(this)
        folderLabel.text = folderName
        folderLabel.setTextColor(Color.BLACK)

        folderLayout.addView(folderIcon)
        folderLayout.addView(folderLabel)

        homeContainer.addView(folderLayout)

        folderIcon.setOnClickListener {
            showAppsInFolder(apps)
        }
    }



    private fun showAppsInFolder(apps: List<String>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_folder_apps)

        val gridLayout = dialog.findViewById<GridLayout>(R.id.gridLayoutApps)
        gridLayout.columnCount = 3

        for (packageName in apps) {
            val appLayout = createAppLayout(packageName)
            gridLayout.addView(appLayout)
        }

        dialog.show()
    }

    private fun createAppLayout(packageName: String): LinearLayout {
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

        imageView.layoutParams = LinearLayout.LayoutParams(100, 100)
        imageView.setOnClickListener {
            // 앱 실행
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        val textView = TextView(this)
        textView.text = packageName
        textView.setTextColor(Color.BLACK)
        textView.textSize = 12f

        appLayout.addView(imageView)
        appLayout.addView(textView)

        return appLayout
    }
}


