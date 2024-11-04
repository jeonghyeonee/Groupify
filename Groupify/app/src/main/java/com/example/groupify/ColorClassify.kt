package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ColorClassify : AppCompatActivity() {

    private lateinit var clusterInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var deviceId: String
    private lateinit var feedbackText: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colorclassify)

        Log.d("suacheck", "ColorClassify onCreate 호출됨")

        // Device ID 가져오기
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("suacheck", "Device ID: $deviceId")

        clusterInput = findViewById(R.id.editTextCluster)
        confirmButton = findViewById(R.id.buttonConfirm)
        feedbackText = findViewById(R.id.feedbackText) // 새로 추가된 TextView 연결

        // confirmButton 클릭 이벤트에서 호출
        confirmButton.setOnClickListener {
            val clusterCount = clusterInput.text.toString()

            if (clusterCount.isNotEmpty()) {
                val kValue = clusterCount.toInt()
                if (kValue in 4..12) {
                    // 유효한 값일 경우 메시지 표시
                    feedbackText.visibility = View.VISIBLE
                    feedbackText.text = "좋아요! $kValue 개로 나누어 드릴게요."
                    sendDataToServer(kValue, deviceId)
                } else {
                    // 유효하지 않은 값일 경우 경고 모달 표시
                    showAlert("4-12 사이의 숫자만 입력해주세요!")
                }
            } else {
                Toast.makeText(this, "클러스터 개수를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 경고 모달 표시 메서드
    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("유효하지 않은 입력")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    // 서버로 k값과 deviceID 전송
    private fun sendDataToServer(kValue: Int, deviceId: String) {
        val url = "http://ec2-13-125-56-70.ap-northeast-2.compute.amazonaws.com:8080/cluster"
        val jsonBody = JSONObject().apply {
            put("k_value", kValue)
            put("device_id", deviceId)
        }

        val body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonBody.toString())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("suacheck", "서버 요청 실패: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@ColorClassify, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                if (responseData != null) {
                    runOnUiThread {
                        Toast.makeText(this@ColorClassify, "서버 응답 완료", Toast.LENGTH_SHORT).show()
                        navigateToFolderLauncherActivity(responseData)
                    }
                } else {
                    Log.e("suacheck", "서버 응답 없음")
                }
            }
        })
    }

    // FolderLauncherActivity로 이동하는 메서드
    private fun navigateToFolderLauncherActivity(responseData: String) {
        if (responseData.isNotEmpty()) {
            val intent = Intent(this, FolderLauncherActivity::class.java)
            intent.putExtra("responseData", responseData)
            startActivity(intent)
        } else {
            Log.e("suacheck", "responseData가 비어 있습니다.")
            Toast.makeText(this, "데이터가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

}
