package com.example.groupify

import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
import org.json.JSONObject

class FolderLauncherActivity : AppCompatActivity() {

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("suacheck", "FolderLauncherActivity onCreate 호출됨 - setContentView 전")

        setContentView(R.layout.activity_folder_launcher)
        Log.d("suacheck", "FolderLauncherActivity onCreate 호출됨 - setContentView 후")

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화

        // 유저의 배경화면 설정
        setUserWallpaper()

        // RecyclerView 설정
        try {
            folderRecyclerView = findViewById(R.id.folderRecyclerView)
            folderRecyclerView.layoutManager = GridLayoutManager(this, 4) // 앱을 4열로 배치
        } catch (e: Exception) {
            Log.e("suacheck", "RecyclerView 초기화 오류", e)
        }

        // 서버에서 받은 클러스터링 결과
        val responseData = intent.getStringExtra("responseData") ?: ""
        Log.d("suacheck", "받은 responseData: $responseData")
        parseAndDisplayClusters(responseData)
    }

    // 뒤로가기 버튼 클릭 시 현재 Activity 종료
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun parseAndDisplayClusters(responseData: String) {
        try {
            val jsonObject = JSONObject(responseData)
            val appArray = jsonObject.getJSONArray("apps")
            val folderMap = mutableMapOf<Int, MutableList<AppData>>()
            Log.d("suacheck", "클러스터 데이터 파싱 시작")

            for (i in 0 until appArray.length()) {
                val app = appArray.getJSONArray(i)
                val packageName = app.getString(0)
                val clusterNumber = app.getInt(1)
                val appInfo = getAppInfo(packageName)

                appInfo?.let {
                    folderMap.getOrPut(clusterNumber) { mutableListOf() }.add(it)
                }
                Log.d("suacheck", "앱 $packageName 클러스터 추가됨")
            }

            folderAdapter = FolderAdapter(folderMap) { clusterNumber, appList ->
                showAppList(clusterNumber, appList)
            }
            folderRecyclerView.adapter = folderAdapter
            Log.d("suacheck", "폴더 어댑터 설정 완료")

        } catch (e: Exception) {
            Log.e("suacheck", "클러스터 데이터 파싱 오류", e)
            Toast.makeText(this, "데이터 파싱 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppList(clusterNumber: Int, appList: List<AppData>) {
        val appAdapter = AppAdapter(appList)
        folderRecyclerView.adapter = appAdapter
        Toast.makeText(this, "Folder $clusterNumber opened", Toast.LENGTH_SHORT).show()
        Log.d("suacheck", "showAppList 호출됨 - Folder $clusterNumber opened")
    }

    private fun getAppInfo(packageName: String): AppData? {
        val packageManager = packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(packageName)
            Log.d("suacheck", "$packageName 앱 정보 가져오기 성공: $appName")
            AppData(packageName, appName, appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("suacheck", "$packageName 앱 정보를 찾을 수 없음", e)
            null
        }
    }

    private fun setUserWallpaper() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)
            val wallpaperDrawable: Drawable? = wallpaperManager.drawable
            wallpaperDrawable?.let {
                folderRecyclerView.background = it
                Log.d("suacheck", "유저의 배경화면 설정 성공")
            } ?: run {
                folderRecyclerView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                Log.d("suacheck", "기본 배경으로 설정됨")
            }
        } catch (e: Exception) {
            Log.e("suacheck", "배경화면 설정 오류", e)
            Toast.makeText(this, "배경화면 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
