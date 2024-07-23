package com.example.groupify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Storage 초기화
        val storage = FirebaseStorage.getInstance("gs://groupify-d9e65.appspot.com")
        storageRef = storage.reference

        // "Hello~" 텍스트뷰 설정
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Hello~"

        // 모든 앱 로그 출력
        val packageManager = packageManager
        val packages = packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val packageName = packageInfo.packageName
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.longVersionCode

            // 로그 출력
            Log.d("AllAppInfo", "App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode")
        }

        // 권한 확인 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
            } else {
                logInstalledApps()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                logInstalledApps()
            }
        }

        // 버튼 설정
        val buttonNext = findViewById<Button>(R.id.button_next)
        buttonNext.setOnClickListener {
            val intent = Intent(this, DeployActivity::class.java)
            startActivity(intent)

            try {
                // 앱 이름과 아이콘을 표시할 컨테이너
                val appContainer: LinearLayout = findViewById(R.id.appContainer)
                val packageManager = packageManager
                val packages = packageManager.getInstalledPackages(0)

                for (packageInfo in packages) {
                    // 구글 플레이 스토어에서 설치된 앱만 필터링
                    if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                        val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                        val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                        // 로그 출력
                        Log.d("AppInfo", "App Name: $appName")

                        // 동적으로 텍스트뷰와 이미지뷰 생성
                        val textView = TextView(this)
                        textView.text = appName

                        val imageView = ImageView(this)
                        imageView.setImageDrawable(appIcon)
                        imageView.layoutParams = LinearLayout.LayoutParams(100, 100) // 아이콘 크기 설정

                        // 컨테이너에 추가
                        val appLayout = LinearLayout(this)
                        appLayout.orientation = LinearLayout.HORIZONTAL
                        appLayout.addView(imageView)
                        appLayout.addView(textView)

                        appContainer.addView(appLayout)
                    }
                }
            } catch (e: Exception) {
                Log.e("AppInfo", "Error retrieving app information", e)
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한이 허용된 경우 로그를 저장
                logInstalledApps()
            } else {
                // 권한이 거부된 경우
                Log.e("Permission", "WRITE_EXTERNAL_STORAGE permission denied")
            }
        }
    }

    private fun logInstalledApps() {
        val packageManager = packageManager
        val packages = packageManager.getInstalledPackages(0)
        val logStringBuilder = StringBuilder()

        for (packageInfo in packages) {
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val packageName = packageInfo.packageName
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.longVersionCode

            // 로그 문자열 생성
            logStringBuilder.append("App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode\n")

            // 로그 출력 (AllAppInfo 태그 사용)
            Log.d("AllAppInfo", "App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode")
        }

        // 로그 저장 및 Firebase에 업로드
        saveLogcat(logStringBuilder.toString())
    }

    private fun saveLogcat(log: String) {
        // 타임스탬프 생성
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        // 로그 파일 경로 설정
        val logsDir = getExternalFilesDir(null)
        if (logsDir != null && !logsDir.exists()) {
            logsDir.mkdirs()
        }

        val logFile = File(logsDir, "logcat_$timestamp.txt")

        try {
            FileOutputStream(logFile, true).use { output ->
                output.write(log.toByteArray())
            }
            Log.d("saveLogcat", "Logcat saved to ${logFile.absolutePath}")

            // Firebase에 로그 파일 업로드
            uploadLogToFirebase(logFile)
        } catch (e: IOException) {
            Log.e("saveLogcat", "Error saving logcat", e)
        }
    }

    private fun uploadLogToFirebase(logFile: File) {
        val fileUri = Uri.fromFile(logFile)
        val storageRef = storageRef.child("logs/${logFile.name}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            Log.d("Firebase", "Log uploaded successfully: ${logFile.name}")
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Failed to upload log", exception)
        }
    }
}
