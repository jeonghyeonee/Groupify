package com.example.groupify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
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
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Storage 초기화
        val storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Firebase App Check 초기화
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // 기기의 ANDROID_ID 가져오기
        deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID)

        // 권한 확인 및 요청
        checkAndRequestPermissions()

        // 버튼 설정
        val buttonNext = findViewById<Button>(R.id.button_next)
        buttonNext.setOnClickListener {
            val intent = Intent(this, DeployActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            logInstalledApps()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                logInstalledApps()
            } else {
                Log.e("Permission", "Required permissions were not granted")
            }
        }
    }

    private fun logInstalledApps() {
        Log.d("LogApps", "Logging installed apps")
        val packageManager = packageManager

        // 모든 설치된 앱을 가져옴
        val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val logStringBuilder = StringBuilder()

        // 앱 이름과 아이콘을 표시할 컨테이너
        val appContainer: LinearLayout = findViewById(R.id.appContainer)

        for (appInfo in installedPackages) {
            val packageName = appInfo.packageName

            // 사용자가 볼 수 있는 앱만 필터링 (시스템 앱 제외)
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                val versionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode

                // 앱 아이콘 가져오기
                val appIcon = appInfo.loadIcon(packageManager)

                // 모든 앱의 로그 문자열 생성
                logStringBuilder.append("App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode\n")

                // 로그 출력
                Log.d("AllAppList", "App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode")

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

        // 모든 앱 로그 저장 및 Firebase에 업로드
        saveLogcat(logStringBuilder.toString(), "logcat_visible_apps")
    }

    private fun saveLogcat(log: String, fileName: String) {
        // 로그 파일 경로 설정
        val logsDir = getExternalFilesDir(null)
        if (logsDir != null && !logsDir.exists()) {
            logsDir.mkdirs()
        }

        val logFile = File(logsDir, "${fileName}_$deviceId.txt")

        try {
            FileOutputStream(logFile, false).use { output ->
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
