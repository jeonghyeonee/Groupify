package com.example.groupify

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ColorClassify : AppCompatActivity() {

    private lateinit var clusterInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var deviceId: String
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster_input)

        Log.d("suacheck", "ClusteringInputActivity onCreate 호출됨")

        // Device ID 가져오기
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("suacheck", "Device ID: $deviceId")

        clusterInput = findViewById(R.id.editTextCluster)
        confirmButton = findViewById(R.id.buttonConfirm)

        // confirmButton 클릭 이벤트에서 호출
        confirmButton.setOnClickListener {
            val clusterCount = clusterInput.text.toString()
            Log.d("suacheck", "Cluster Count 입력값: $clusterCount")

            if (clusterCount.isNotEmpty()) {
                val kValue = clusterCount.toInt()  // k 값 정수로 변환
                Log.d("suacheck", "kValue 변환 성공: $kValue")

                // 서버로 k값과 deviceID 전송
                sendDataToServer(kValue, deviceId)
            } else {
                Toast.makeText(this, "클러스터 개수를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 서버로 k값과 deviceID 전송
    private fun sendDataToServer(kValue: Int, deviceId: String) {
        val url = "http://ec2-13-125-56-70.ap-northeast-2.compute.amazonaws.com:8080/cluster"
        Log.d("suacheck", "서버 URL: $url")

        val jsonBody = JSONObject().apply {
            put("k_value", kValue)
            put("device_id", deviceId)
        }

        val jsonString = jsonBody.toString()
        Log.d("suacheck", "서버로 보낼 JSON 데이터: $jsonString")

        val body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonString)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

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
                    Log.d("suacheck", "서버 응답 데이터: $responseData")
                    runOnUiThread {
                        Toast.makeText(this@ColorClassify, "서버 응답 완료", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("suacheck", "서버 응답 없음")
                }
            }
        })
    }
}
