package com.example.groupify

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
import com.example.groupify.models.FolderData

class LauncherActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appAdapter: AppAdapter
    private val appList = mutableListOf<Any>() // 앱과 폴더를 함께 담을 MutableList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.launcherRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        loadInstalledApps() // 기기에서 설치된 앱 불러오기
        setupRecyclerView()
    }

    private fun loadInstalledApps() {
        // 모든 앱 불러오기
        val pm: PackageManager = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            val appName = pm.getApplicationLabel(packageInfo).toString()
            val appIcon = pm.getApplicationIcon(packageInfo.packageName)
            appList.add(AppData(packageInfo.packageName, appName, appIcon))
        }
    }

    private fun setupRecyclerView() {
        appAdapter = AppAdapter(appList, ::onAppClick, ::onFolderClick)
        recyclerView.adapter = appAdapter

        // 드래그 앤 드롭을 위한 ItemTouchHelper 설정
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

                // 앱 리스트에서 위치 변경
                val movedItem = appList.removeAt(fromPos)
                appList.add(toPos, movedItem)
                appAdapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    // 앱 아이콘 클릭 시 앱 실행
    private fun onAppClick(appData: AppData) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(appData.packageName)
            startActivity(launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "${appData.appName} 실행 실패", Toast.LENGTH_SHORT).show()
            Log.e("LauncherActivity", "${appData.packageName} 실행 오류", e)
        }
    }

    // 폴더 클릭 시 내부 앱 리스트 표시
    private fun onFolderClick(folderData: FolderData) {
        Toast.makeText(this, "${folderData.folderName} 폴더 열기", Toast.LENGTH_SHORT).show()
    }
}
