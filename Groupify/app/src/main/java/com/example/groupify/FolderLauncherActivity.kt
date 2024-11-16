// FolderLauncherActivity.kt

package com.example.groupify

import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.groupify.models.AppData
import org.json.JSONObject

class FolderLauncherActivity : AppCompatActivity() {

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var appAdapter: AppAdapter
    private lateinit var nameButton: Button
    private lateinit var emojiButton: Button
    private lateinit var fruitButton: Button



    private var isNameMode = true // 기본적으로 이름 모드
    private var isFruitMode = false // 과일 모드 상태 추가
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_launcher)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // 뒤로가기 버튼 클릭 시 이전 액티비티로 돌아가기
        }


        // 버튼 초기화
        nameButton = findViewById(R.id.nameButton)
        emojiButton = findViewById(R.id.emojiButton)
        fruitButton = findViewById(R.id.fruitButton)

        // '이름별' 버튼 클릭 시
        nameButton.setOnClickListener {
            isNameMode = true
            updateFolderNames()
        }

        // '이모티콘' 버튼 클릭 시
        emojiButton.setOnClickListener {
            isNameMode = false
            isFruitMode = false // 과일 모드 해제
            updateFolderNames()
        }

        // '과일' 버튼 클릭 시
        fruitButton.setOnClickListener {
            isFruitMode = true
            isNameMode = false // 이름 모드 해제
            updateFolderNames()
        }

        // RecyclerView 설정
        folderRecyclerView = findViewById(R.id.folderRecyclerView)
        folderRecyclerView.layoutManager = GridLayoutManager(this, 4) // 앱을 4열로 배치

        // 'Intent'로 전달된 데이터 받기
        val responseData = intent.getStringExtra("responseData") ?: ""
        parseAndDisplayClusters(responseData)

        // '다음' 버튼 클릭 시
        // FolderLauncherActivity.kt
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, LauncherActivity::class.java)

            // 현재 폴더 이름 모드 상태 전달
            intent.putExtra("isNameMode", isNameMode)
            intent.putExtra("isFruitMode", isFruitMode)

            // 서버에서 받은 responseData 그대로 넘겨줌
            intent.putExtra("responseData", responseData)

            Log.d("suacheck", "Sending responseData: $responseData")

            // LauncherActivity로 전환
            startActivity(intent)
        }


    }




    private fun updateFolderNames() {
        folderAdapter.updateFolderNames(isNameMode, isFruitMode) // FolderAdapter에 모드 변경을 전달
    }

    private fun parseAndDisplayClusters(responseData: String) {
        try {
            val jsonObject = JSONObject(responseData)
            val appArray = jsonObject.getJSONArray("apps")
            val folderMap = mutableMapOf<String, MutableList<AppData>>() // predicted_color로 그룹화
            val clusterMap = mutableMapOf<Int, MutableList<AppData>>()

            for (i in 0 until appArray.length()) {
                val app = appArray.getJSONObject(i)
                val packageName = app.getString("app_name")
                val predictedColor = app.getString("predicted_color")
                val appInfo = getAppInfo(packageName)

                appInfo?.let {
                    val clusterNumber = app.getInt("predicted_cluster")
                    clusterMap.getOrPut(clusterNumber) { mutableListOf() }.add(it.copy(predictedColor = predictedColor))

                    // 로그: 파싱된 앱의 정보 출력
                    Log.d("suacheck", "App: $packageName, Predicted Cluster: $clusterNumber, Predicted Color: $predictedColor")
                }
            }

            // 클러스터별로 predicted_color를 폴더 이름으로 사용
            clusterMap.forEach { (clusterNumber, apps) ->
                val firstApp = apps.first()
                folderMap[firstApp.predictedColor] = apps

                // 로그: folderMap에 담긴 클러스터 데이터 출력
                Log.d("suacheck", "Cluster: $clusterNumber, Folder Color: ${firstApp.predictedColor}, Apps: ${apps.size}")
            }

            // 폴더 어댑터 설정
            folderAdapter = FolderAdapter(folderMap, isNameMode, isFruitMode) { predictedColor, appList ->
                showAppList(predictedColor, appList)
            }
            folderRecyclerView.adapter = folderAdapter

        } catch (e: Exception) {
            Log.e("suacheck", "클러스터 데이터 파싱 오류", e)
            Toast.makeText(this, "데이터 파싱 오류", Toast.LENGTH_SHORT).show()
        }
    }




    private fun showAppList(predictedColor: String, appList: List<AppData>) {
        Log.d("FolderLauncherActivity", "showAppList called for color: $predictedColor")

        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_folder_launcher_folderview, null)
        val folderTitle: TextView = dialogView.findViewById(R.id.folderTitle)
        val viewPager: ViewPager2 = dialogView.findViewById(R.id.viewPager)

        folderTitle.text = predictedColor // 폴더 이름을 설정

        // 한 페이지에 최대 9개의 앱이 보이도록 설정 (3x3)
        val maxIcons = 9
        val iconCount = appList.size
        val rowCount = (iconCount / 3) + if (iconCount % 3 == 0) 0 else 1 // 앱 개수에 맞춰 rowCount 설정
        // 앱 목록을 최대 9개씩 나누어 페이지네이션을 만들기 위해 9개씩 그룹화
        val paginatedAppList = appList.chunked(9)

        // ViewPager2에 어댑터 설정
        val adapter = AppPagerAdapter(paginatedAppList)
        viewPager.adapter = adapter

        // 다이얼로그 설정
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 다이얼로그 배경을 투명으로 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }




    private fun createAppLayout(app: AppData): LinearLayout {
        val appLayout = LayoutInflater.from(this).inflate(R.layout.item_app, null) as LinearLayout

        // 앱 아이콘 설정
        val appIconView: ImageView = appLayout.findViewById(R.id.appIcon)
        appIconView.setImageDrawable(app.appIcon)

        // 앱 이름 설정
        val appLabel: TextView = appLayout.findViewById(R.id.appLabel)
        appLabel.text = app.appName

        // 앱 클릭 시 실행되지 않도록 설정
        appLayout.setOnClickListener {
            // 앱을 실행하지 않음
        }

        return appLayout
    }





    private fun getAppInfo(packageName: String): AppData? {
        val packageManager = packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(packageName)

            AppData(packageName, appName, appIcon, 0, "")
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}

private fun Intent.putExtra(s: String, clusteredApps: Map<String, List<AppData>>) {

}
