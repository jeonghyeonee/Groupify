package com.example.groupify

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.provider.Settings.Secure


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
        val buttonFunction = findViewById<Button>(R.id.button_function)
        buttonFunction.setOnClickListener {
            val intent = Intent(this, FunctionClassify::class.java)
            startActivity(intent)
        }

        // 색상 선택 버튼 설정
        val buttonColorRange = findViewById<Button>(R.id.button_color_range)
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
                    // 로그인 성공
                    val user: FirebaseUser? = auth.currentUser
                    Log.d("FirebaseAuth", "signInAnonymously:success")

                    // 사용자 토큰 가져오기
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result?.token
                            Log.d("FirebaseAuth", "Token: $token")

                            // 이 토큰을 사용하여 Firebase Storage에 접근할 수 있습니다.
                            accessFirebaseWithToken(token)
                        } else {
                            Log.w("FirebaseAuth", "Token retrieval failed", tokenTask.exception)
                        }
                    }
                } else {
                    // 로그인 실패
                    Log.w("FirebaseAuth", "signInAnonymously:failure", task.exception)
                }
            }
    }

    private fun accessFirebaseWithToken(token: String?) {
        if (token != null) {
            // 토큰을 사용하여 Firebase Storage 또는 다른 Firebase 서비스에 접근
            // 예시로 StorageRef에서 직접 사용할 필요는 없지만
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileRef = storageRef.child("logs/myfile.json")

            // 업로드 작업을 예시로 진행할 수 있음
            val fileUri = Uri.fromFile(File("/path/to/your/file"))
            fileRef.putFile(fileUri).addOnSuccessListener {
                Log.d("FirebaseStorage", "Upload success")
            }.addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Upload failed", exception)
            }
        } else {
            Log.e("FirebaseAuth", "Token is null, cannot access Firebase")
        }
    }



    // 권한 확인 및 요청
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 1)
            } else {
                logInstalledApps()  // 앱 목록 동기화 로직
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1)
            } else {
                logInstalledApps()  // 앱 목록 동기화 로직
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            logInstalledApps()  // 앱 목록 동기화 로직
        }
    }

    private fun logInstalledApps() {
        val packageManager = packageManager
        val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appDataList = mutableListOf<Map<String, Any>>() // JSON 데이터로 저장할 리스트

        for (appInfo in installedPackages) {
            val packageName = appInfo.packageName
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                val versionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
                val appIcon = appInfo.loadIcon(packageManager)
                val dominantColorHex = try {
                    getDominantColorHex(appIcon)
                } catch (e: Exception) {
                    Log.e("AppLog", "Error extracting dominant color for $appName", e)
                    "#000000"
                }

                // 각 앱의 정보를 Map으로 변환하여 JSON 형태로 저장
                val appData = mapOf(
                    "appName" to appName,
                    "packageName" to packageName,
                    "versionName" to versionName,
                    "versionCode" to versionCode,
                    "dominantColor" to dominantColorHex
                )

                // 리스트에 추가
                appDataList.add(appData)

                // 'suacheck' 태그로 로그 찍기
                Log.d("suacheck", "App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode, Dominant Color: $dominantColorHex")
            }
        }

        // Firebase에 JSON 데이터로 저장
        saveToFirebaseAsJson(appDataList)
        Log.d("suacheck", "firebase에 업로드 되었습니다")
    }


    private fun saveToFirebaseAsJson(appDataList: List<Map<String, Any>>) {
        val gson = com.google.gson.Gson()  // Gson 객체 생성

        // appDataList를 JSON 문자열로 변환
        val jsonString = gson.toJson(appDataList)

        val logFile = File(getExternalFilesDir(null), "app_data_$deviceId.json")
        Log.d("suacheck", "Saving file to: ${logFile.absolutePath}")

        try {
            FileOutputStream(logFile, false).use { output ->
                output.write(jsonString.toByteArray())
            }
            Log.d("suacheck", "JSON data saved to ${logFile.absolutePath}")
            uploadLogToFirebase(logFile)  // Firebase에 업로드
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




    private fun getDominantColorHex(drawable: Drawable): String {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val colorFrequencyMap: MutableMap<Int, Int> = HashMap()

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixelColor = bitmap.getPixel(x, y)
                if (Color.alpha(pixelColor) < 255 || isNearWhiteOrBlack(pixelColor)) continue
                val colorCount = colorFrequencyMap[pixelColor] ?: 0
                colorFrequencyMap[pixelColor] = colorCount + 1
            }
        }
        val dominantColor = colorFrequencyMap.maxByOrNull { it.value }?.key ?: Color.GRAY
        return String.format("#%06X", 0xFFFFFF and dominantColor)
    }

    private fun isNearWhiteOrBlack(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return (red > 245 && green > 245 && blue > 245) || (red < 10 && green < 10 && blue < 10)
    }
}
