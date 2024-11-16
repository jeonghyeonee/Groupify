package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
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

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // 뒤로가기 버튼 클릭 시 이전 액티비티로 돌아가기
        }


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
                    feedbackText.text = "Great! \n We will categorize it into $kValue folders \uD83D\uDE04"
                    sendDataToServer(kValue, deviceId)
                } else {
                    // 유효하지 않은 값일 경우 경고 모달 표시
                    showAlert("Please enter a number between 4 and 12 !")
                }
            } else {
                Toast.makeText(this, "Please enter the number of clusters.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 경고 모달 표시 메서드
    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Invalid input")
            .setMessage(message)
            .setPositiveButton("Confirm", null)
            .show()
    }

    // 서버로 k값과 deviceID 전송
    private fun sendDataToServer(kValue: Int, deviceId: String) {
        val url = "http://ec2-13-125-56-70.ap-northeast-2.compute.amazonaws.com:8080/cluster"
        val jsonBody = JSONObject().apply {
            put("k_value", kValue)
            put("device_id", deviceId)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody.toString())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("suacheck", "서버 요청 실패: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@ColorClassify, "Server request failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    runOnUiThread {
                        Toast.makeText(this@ColorClassify, "Success ! ", Toast.LENGTH_SHORT).show()
                        // 서버 응답 후 'FolderLauncherActivity'로 데이터 전달
                        navigateToFolderLauncherActivity(responseData)
                    }
                } else {
                    Log.e("suacheck", "서버 응답 없음")
                }
            }
        })
    }

    // 'FolderLauncherActivity'로 데이터 전달 및 화면 이동
    private fun navigateToFolderLauncherActivity(responseData: String) {
        try {
            // 서버 응답에서 'apps' 배열을 파싱
            val jsonObject = JSONObject(responseData)
            val appsArray: JSONArray = jsonObject.getJSONArray("apps")

            // 각 앱에 대해 클러스터 및 색상 출력
            for (i in 0 until appsArray.length()) {
                val appObject = appsArray.getJSONObject(i)
                val appName = appObject.getString("app_name")
                val predictedCluster = appObject.getInt("predicted_cluster")
                val predictedColor = appObject.getString("predicted_color")

                // 디버깅용 로그 출력
                Log.d("suacheck", "App: $appName, Predicted Cluster: $predictedCluster, Predicted Color: $predictedColor")
            }

            // FolderLauncherActivity로 데이터를 넘겨서 화면을 전환
            val intent = Intent(this, FolderLauncherActivity::class.java)
            intent.putExtra("responseData", responseData) // 응답 데이터를 전달
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("suacheck", "클러스터 데이터 파싱 오류", e)
            Toast.makeText(this, "Cluster data parsing error", Toast.LENGTH_SHORT).show()
        }
    }


    // 클러스터 데이터 로그로 출력
    private fun logClusterData(responseData: String) {
        try {
            // 서버 응답에서 'apps' 배열을 파싱
            val jsonObject = JSONObject(responseData)
            val appsArray: JSONArray = jsonObject.getJSONArray("apps")

            // 각 앱에 대해 클러스터 및 색상 출력
            for (i in 0 until appsArray.length()) {
                val appObject = appsArray.getJSONObject(i)
                val appName = appObject.getString("app_name")
                val predictedCluster = appObject.getInt("predicted_cluster")
                val predictedColor = appObject.getString("predicted_color")

                // 결과 출력 (디버깅용)
                Log.d("suacheck", "App: $appName, Predicted Cluster: $predictedCluster, Predicted Color: $predictedColor")
            }
        } catch (e: Exception) {
            Log.e("suacheck", "클러스터 데이터 파싱 오류", e)
            Toast.makeText(this, "Cluster data parsing error", Toast.LENGTH_SHORT).show()
        }
    }
}
