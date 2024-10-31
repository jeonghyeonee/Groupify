package com.example.groupify

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.provider.Settings.Secure
import android.widget.LinearLayout


class SelectionActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    private lateinit var deviceId: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        // FirebaseAuth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // 익명 로그인 시도
        signInAnonymously()

        // Firebase Storage 초기화
        val storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // 기기의 ANDROID_ID 가져오기
        deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID)

        // 동기화 버튼 설정
        val buttonSync = findViewById<Button>(R.id.button_sync)
        buttonSync.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("앱 목록 동기화")
                .setMessage("앱 목록을 업데이트 하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    // 사용자가 확인을 누르면 앱 목록 동기화 시작
                    checkAndRequestPermissions()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 기능 선택 버튼 설정
        val buttonFunction = findViewById<LinearLayout>(R.id.button_function)
        buttonFunction.setOnClickListener {
            val intent = Intent(this, FunctionClassify::class.java)
            startActivity(intent)
        }

        // 색상 선택 버튼 설정
        val buttonColorRange = findViewById<LinearLayout>(R.id.button_color_range)
        buttonColorRange.setOnClickListener {
            val intent = Intent(this, ColorClassify::class.java)
            Log.d("suacheck", "color 가 선택되었습니다")
            startActivity(intent)
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    Log.d("FirebaseAuth", "signInAnonymously:success")
                } else {
                    Log.w("FirebaseAuth", "signInAnonymously:failure", task.exception)
                }
            }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 1)
            } else {
                logInstalledApps()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1)
            } else {
                logInstalledApps()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            logInstalledApps()
        }
    }

    private fun logInstalledApps() {
        val packageManager = packageManager
        val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appDataList = mutableListOf<Map<String, Any>>()

        for (appInfo in installedPackages) {
            val packageName = appInfo.packageName
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                val versionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
                val appIcon = appInfo.loadIcon(packageManager)
                val dominantColorHex = try {
                    extractDominantColorHex(appIcon)
                } catch (e: Exception) {
                    Log.e("AppLog", "Error extracting dominant color for $appName", e)
                    "#000000"
                }

                val appData = mapOf(
                    "appName" to appName,
                    "packageName" to packageName,
                    "versionName" to versionName,
                    "versionCode" to versionCode,
                    "dominantColor" to dominantColorHex
                )
                appDataList.add(appData)

                Log.d("suacheck", "App Name: $appName, Package Name: $packageName, Dominant Color: $dominantColorHex")
            }
        }

        saveToFirebaseAsJson(appDataList)
        Log.d("suacheck", "firebase에 업로드 되었습니다")
    }

    private fun extractDominantColorHex(drawable: Drawable): String {
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is AdaptiveIconDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            else -> throw IllegalArgumentException("Unsupported drawable type")
        }

        return extractDominantColorFromBitmap(bitmap)
    }

    private fun extractDominantColorFromBitmap(bitmap: Bitmap): String {
        val palette = Palette.from(bitmap).generate()
        val dominantColor = palette.getDominantColor(Color.GRAY)
        return String.format("#%06X", 0xFFFFFF and dominantColor)
    }

    private fun saveToFirebaseAsJson(appDataList: List<Map<String, Any>>) {
        val gson = com.google.gson.Gson()
        val jsonString = gson.toJson(appDataList)

        val logFile = File(getExternalFilesDir(null), "app_data_$deviceId.json")
        Log.d("suacheck", "Saving file to: ${logFile.absolutePath}")

        try {
            FileOutputStream(logFile, false).use { output ->
                output.write(jsonString.toByteArray())
            }
            Log.d("suacheck", "JSON data saved to ${logFile.absolutePath}")
            uploadLogToFirebase(logFile)
        } catch (e: IOException) {
            Log.e("saveToFirebaseAsJson", "Error saving JSON", e)
        }
    }

    private fun uploadLogToFirebase(logFile: File) {
        val fileUri = Uri.fromFile(logFile)
        val storageRef = storageRef.child("logs/${logFile.name}")
        Log.d("suacheck", "Uploading file to Firebase path: logs/${logFile.name}")

        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            Log.d("Firebase", "JSON data uploaded successfully: ${logFile.name}")
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Failed to upload JSON data", exception)
        }
    }
}
