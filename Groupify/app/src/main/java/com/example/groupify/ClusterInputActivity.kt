package com.example.groupify

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ClusterInputActivity : AppCompatActivity() {

    private lateinit var clusterInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster_input)

        // Device ID 가져오기
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        clusterInput = findViewById(R.id.editTextCluster)
        confirmButton = findViewById(R.id.buttonConfirm)

        confirmButton.setOnClickListener {
            val clusterCount = clusterInput.text.toString()

            if (clusterCount.isNotEmpty()) {
                // Firebase에 업로드 시 파일명에 deviceId와 clusterCount 포함
                val logFileName = "logcat_SUA2_apps_${deviceId}_K${clusterCount}.txt"
                saveAndUploadLog(logFileName, clusterCount)

                // ClusteringActivity로 이동하며 파일명과 K 값을 전달
                val intent = Intent(this, ClusteringActivity::class.java)
                intent.putExtra("logFileName", logFileName)  // 생성된 파일명 전달
                intent.putExtra("clusterCount", clusterCount)  // K 값 전달
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "클러스터 개수를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 로그 파일 저장 및 Firebase에 업로드
    private fun saveAndUploadLog(logFileName: String, clusterCount: String) {
        val packageManager = packageManager
        val installedPackages = packageManager.getInstalledApplications(0)

        val logStringBuilder = StringBuilder()

        for (appInfo in installedPackages) {
            val packageName = appInfo.packageName

            // 사용자에게 표시되는 앱만 필터링
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val appIcon = appInfo.loadIcon(packageManager)
                val dominantColorHex = getDominantColorHex(appIcon)

                // 로그 형식 지정
                logStringBuilder.append("App Name: $appName, Package Name: $packageName, Dominant Color: $dominantColorHex\n")
            }
        }

        // 파일 저장
        val logsDir = getExternalFilesDir(null)
        val logFile = File(logsDir, logFileName)

        try {
            FileOutputStream(logFile, false).use { output ->
                output.write(logStringBuilder.toString().toByteArray())
            }
            Log.d("saveLogcat", "Logcat saved to ${logFile.absolutePath}")

            // Firebase에 업로드
            uploadToFirebase(logFile, clusterCount)
        } catch (e: IOException) {
            Log.e("saveLogcat", "Error saving logcat", e)
        }
    }

    // Firebase에 파일 업로드
    private fun uploadToFirebase(logFile: File, clusterCount: String) {
        val fileUri = Uri.fromFile(logFile)
        val storageRef = FirebaseStorage.getInstance().reference.child("logs/${logFile.name}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            Log.d("FirebaseUpload", "File uploaded successfully: ${logFile.name}")
            Toast.makeText(this, "클러스터링 파일 업로드 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpload", "Failed to upload file", exception)
            Toast.makeText(this, "파일 업로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 앱 아이콘에서 Dominant Color 추출
    private fun getDominantColorHex(drawable: Drawable): String {
        val bitmap = drawableToBitmap(drawable)

        val colorFrequencyMap: MutableMap<Int, Int> = HashMap()

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixelColor = bitmap.getPixel(x, y)

                // 투명한 픽셀 무시
                if (Color.alpha(pixelColor) < 255) continue

                // 거의 흰색 또는 검은색 무시
                if (isNearWhiteOrBlack(pixelColor)) continue

                val colorCount = colorFrequencyMap[pixelColor] ?: 0
                colorFrequencyMap[pixelColor] = colorCount + 1
            }
        }

        val dominantColor = colorFrequencyMap.maxByOrNull { it.value }?.key ?: Color.GRAY

        return String.format("#%06X", 0xFFFFFF and dominantColor)
    }

    // Drawable을 Bitmap으로 변환
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // 흰색 또는 검은색에 가까운 색상 무시
    private fun isNearWhiteOrBlack(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val nearWhiteThreshold = 245
        val nearBlackThreshold = 10
        return (red > nearWhiteThreshold && green > nearWhiteThreshold && blue > nearWhiteThreshold) ||
                (red < nearBlackThreshold && green < nearBlackThreshold && blue < nearBlackThreshold)
    }
}
