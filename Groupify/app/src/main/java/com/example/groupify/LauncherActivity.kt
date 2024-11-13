package com.example.groupify

import android.app.WallpaperManager
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
import org.json.JSONObject

class LauncherActivity : AppCompatActivity() {

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val isNameMode = intent.getBooleanExtra("isNameMode", true)
        val isFruitMode = intent.getBooleanExtra("isFruitMode", false)
        val responseData = intent.getStringExtra("responseData") ?: ""

        Log.d("suacheck", "Received responseData: $responseData")
        Log.d("suacheck", "Name Mode: $isNameMode, Fruit Mode: $isFruitMode")

        // RecyclerView 설정
        folderRecyclerView = findViewById(R.id.folderRecyclerView)
        folderRecyclerView.layoutManager = GridLayoutManager(this, 3) // 3개의 폴더를 한 줄에 배치


        val wallpaperManager = WallpaperManager.getInstance(this)
        val currentWallpaper = wallpaperManager.drawable // 현재 배경화면을 가져옴
        val backgroundImageView = findViewById<ImageView>(R.id.backgroundImageView)
        backgroundImageView.setImageDrawable(currentWallpaper)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val wallpaperDrawable = wallpaperManager.drawable
            backgroundImageView.setImageDrawable(wallpaperDrawable)  // ImageView에 설정
        } else {
            val wallpaperBitmap = wallpaperManager.drawable?.toBitmap()
            backgroundImageView.setImageBitmap(wallpaperBitmap)
        }

        val appIcon = findViewById<ImageView>(R.id.appIcon)

        // 아이콘 드래그 가능한 설정
        appIcon.setOnLongClickListener { view ->
            val clipData = ClipData.newPlainText("app", view.tag.toString())
            val dragShadowBuilder = View.DragShadowBuilder(view)
            view.startDrag(clipData, dragShadowBuilder, null, 0)
            true
        }



        // 서버에서 받은 responseData를 파싱하여 폴더 생성
        responseData?.let {
            displayFolders(it, isNameMode, isFruitMode)
        }
    }


    private fun displayFolders(responseData: String, isNameMode: Boolean, isFruitMode: Boolean) {
        val folderMap = mutableMapOf<String, MutableList<AppData>>() // predicted_color로 그룹화

        val jsonObject = JSONObject(responseData)
        val appArray = jsonObject.getJSONArray("apps")

        for (i in 0 until appArray.length()) {
            val app = appArray.getJSONObject(i)
            val packageName = app.getString("app_name")
            val predictedColor = app.getString("predicted_color")
            val appInfo = getAppInfo(packageName)

            appInfo?.let {
                folderMap.getOrPut(predictedColor) { mutableListOf() }.add(it)
            }
        }

        // 폴더 어댑터 설정
        folderAdapter = FolderAdapter(folderMap, isNameMode, isFruitMode) { predictedColor, appList ->
            showFolderDialog(appList)
        }
        folderRecyclerView.adapter = folderAdapter
    }

    private fun createFolderLayout(predictedColor: String, appList: List<AppData>, isNameMode: Boolean, isFruitMode: Boolean): LinearLayout {
        val folderLayout = LayoutInflater.from(this).inflate(R.layout.item_folder, null) as LinearLayout

        // 폴더 이름 설정
        val folderLabel: TextView = folderLayout.findViewById(R.id.folderLabel)
        folderLabel.text = when {
            isNameMode -> predictedColor
            isFruitMode -> getFruitEmojiForColor(predictedColor)
            else -> getEmojiForColor(predictedColor)
        }

        // 폴더 아이콘 설정 (기존 아이콘을 사용)
        val folderPreviewGrid: GridLayout = folderLayout.findViewById(R.id.folderPreviewGrid)

        // 폴더의 앱 아이콘을 최대 9개까지 그리드로 설정
        folderPreviewGrid.removeAllViews()
        val maxIcons = 9
        val iconCount = minOf(appList.size, maxIcons)

        for (i in 0 until iconCount) {
            val appIconView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 50
                    height = 50
                }
                setImageDrawable(appList[i].appIcon)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            folderPreviewGrid.addView(appIconView)
        }

        // 폴더 클릭 시 앱 목록을 보여줌
        folderLayout.setOnClickListener {
            showFolderDialog(appList)
        }

        return folderLayout
    }

    private fun showFolderDialog(appList: List<AppData>) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Apps in this Folder")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        appList.forEach { app ->
            val appLayout = createAppLayout(app)
            layout.addView(appLayout)
        }

        dialog.setView(layout)
        dialog.setNegativeButton("Close", null)
        dialog.show()
    }

    private fun createAppLayout(app: AppData): LinearLayout {
        val appLayout = LinearLayout(this)
        appLayout.orientation = LinearLayout.HORIZONTAL
        appLayout.setPadding(8, 8, 8, 8)

        val imageView = ImageView(this)
        imageView.setImageDrawable(app.appIcon)

        val textView = TextView(this)
        textView.text = app.appName
        textView.setPadding(16, 0, 0, 0)

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱 클릭 시 실행
        appLayout.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            launchIntent?.let { startActivity(it) }
        }

        return appLayout
    }

    private fun getEmojiForColor(color: String): String {
        return when (color) {
            "red" -> "❤️"
            "yellow" -> "💛"
            "green" -> "💚"
            "blue" -> "💙"
            "white" -> "🤍"
            "purple" -> "💜"
            "pink" -> "🩷"
            "orange" -> "🧡"
            "black" -> "🖤"
            "brown" -> "🤎"
            else -> color
        }
    }

    private fun getFruitEmojiForColor(color: String): String {
        return when (color) {
            "red" -> "🍎"
            "yellow" -> "🍌"
            "green" -> "🥑"
            "blue" -> "🫐"
            "white" -> "🥛"
            "purple" -> "🍇"
            "pink" -> "🍑"
            "orange" -> "🍊"
            "black" -> "🍙"
            "brown" -> "🍫"
            else -> color
        }
    }

    private fun getAppInfo(packageName: String): AppData? {
        val packageManager = packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(packageName) // Drawable 반환

            AppData(packageName, appName, appIcon, 0, "")
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
