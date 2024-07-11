package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // "Hello~" 텍스트뷰 설정
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Hello~"

        // Get All App Info
        logInstalledApps()

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
        }

        // 로그 저장
        saveLogcat(logStringBuilder.toString())
    }

    private fun saveLogcat(log: String) {
        // 타임스탬프 생성
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())


        // 로그 파일 경로 설정
        val projectDir = BuildConfig.PROJECT_DIR
        val logsDir = if (BuildConfig.IS_WINDOWS) {
            File("$projectDir\\logs")
        } else {
            File("$projectDir/logs")
        }

        if (!logsDir.exists()) {
            logsDir.mkdirs()
        }

        val logFile = if (BuildConfig.IS_WINDOWS) {
            File("logsDir\\logcat_$timestamp.txt")
        } else {
            File(logsDir, "logcat_$timestamp.txt")
        }

        try {
            FileOutputStream(logFile, true).use { output ->
                output.write(log.toByteArray())
            }
            Log.d("saveLogcat", "Logcat saved to ${logFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("saveLogcat", "Error saving logcat", e)
        }
    }
}
