package com.example.groupify

import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
import org.json.JSONObject

class FolderLauncherActivity : AppCompatActivity() {

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private val folderMap = mutableMapOf<Int, MutableList<AppData>>() // 폴더 데이터를 관리할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_launcher)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화

        // RecyclerView 초기화
        folderRecyclerView = findViewById(R.id.folderRecyclerView)
        folderRecyclerView.layoutManager = GridLayoutManager(this, 4) // 앱을 4열로 배치

        // 배경화면 설정
        setUserWallpaper()

        // 서버에서 받은 클러스터링 결과 로드
        val responseData: String = intent.getStringExtra("responseData") ?: ""
        val sharedPreferences = getSharedPreferences("folder_data", MODE_PRIVATE)

        if (responseData.isNotEmpty()) {
            sharedPreferences.edit().putString("responseData", responseData).apply()
        }

        val savedData: String = sharedPreferences.getString("responseData", "") ?: ""
        parseAndDisplayClusters(if (savedData.isNotEmpty()) savedData else responseData)

        // "앱 종료하기" 버튼
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            val intent = Intent(this, LauncherActivity::class.java)
            intent.putExtra("responseData", responseData)
            startActivity(intent)
            finish()
        }

        // 드래그 앤 드롭 기능 설정
        setupDragAndDrop()
    }

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
            folderMap.clear()

            for (i in 0 until appArray.length()) {
                val app = appArray.getJSONArray(i)
                val packageName = app.getString(0)
                val clusterNumber = app.getInt(1)
                val appInfo = getAppInfo(packageName)

                appInfo?.let {
                    folderMap.getOrPut(clusterNumber) { mutableListOf() }.add(it)
                }
            }

            folderAdapter = FolderAdapter(folderMap, onFolderClick = { clusterNumber, appList ->
                showAppList(clusterNumber, appList)
            })

            folderRecyclerView.adapter = folderAdapter
        } catch (e: Exception) {
            Log.e("FolderLauncherActivity", "클러스터 데이터 파싱 오류", e)
            Toast.makeText(this, "데이터 파싱 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppList(clusterNumber: Int, appList: List<AppData>) {
        Toast.makeText(this, "폴더 $clusterNumber 열기", Toast.LENGTH_SHORT).show()
        appList.forEach { appData ->
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(appData.packageName)
                launchIntent?.let { startActivity(it) }
            } catch (e: Exception) {
                Log.e("FolderLauncherActivity", "${appData.packageName} 실행 오류", e)
                Toast.makeText(this, "${appData.appName} 실행 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAppInfo(packageName: String): AppData? {
        val packageManager = packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(packageName)
            AppData(packageName, appName, appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("FolderLauncherActivity", "$packageName 앱 정보를 찾을 수 없음", e)
            null
        }
    }

    private fun setUserWallpaper() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)
            val wallpaperDrawable: Drawable? = wallpaperManager.drawable
            wallpaperDrawable?.let {
                folderRecyclerView.background = it
            } ?: run {
                folderRecyclerView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            }
        } catch (e: Exception) {
            Log.e("FolderLauncherActivity", "배경화면 설정 오류", e)
            Toast.makeText(this, "배경화면 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDragAndDrop() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                // 데이터 위치도 이동
                val movedItem = folderMap.keys.elementAt(fromPos)
                folderMap.remove(movedItem)?.let { appList ->
                    folderMap.put(movedItem, appList)
                }

                folderAdapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(folderRecyclerView)
    }
}
