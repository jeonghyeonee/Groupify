package com.example.groupify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FunctionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView  // RecyclerView 선언

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster_select)

        // RecyclerView 초기화

        // 버튼 설정 (4부터 12까지 클러스터 개수 선택)
        findViewById<Button>(R.id.btnCluster4).setOnClickListener { requestCluster(4) }
        findViewById<Button>(R.id.btnCluster5).setOnClickListener { requestCluster(5) }
        findViewById<Button>(R.id.btnCluster6).setOnClickListener { requestCluster(6) }
        findViewById<Button>(R.id.btnCluster7).setOnClickListener { requestCluster(7) }
        findViewById<Button>(R.id.btnCluster8).setOnClickListener { requestCluster(8) }
        findViewById<Button>(R.id.btnCluster9).setOnClickListener { requestCluster(9) }
        findViewById<Button>(R.id.btnCluster10).setOnClickListener { requestCluster(10) }
        findViewById<Button>(R.id.btnCluster11).setOnClickListener { requestCluster(11) }
        findViewById<Button>(R.id.btnCluster12).setOnClickListener { requestCluster(12) }
    }

    // 클러스터링 요청
    // 클러스터링 요청
    private fun requestCluster(nClusters: Int) {
        RetrofitClient.apiService.getClusteredApps(ClusterRequest(nClusters))
            .enqueue(object : Callback<ClusterResponse> {
                override fun onResponse(
                    call: Call<ClusterResponse>,
                    response: Response<ClusterResponse>
                ) {
                    if (response.isSuccessful) {
                        val clusterMap = response.body()?.clusters as? Map<String, List<Map<String, Any>>>
                        if (clusterMap == null) {
                            Log.e("API_RESPONSE", "Invalid cluster data format")
                            Toast.makeText(this@FunctionActivity, "클러스터링 데이터 오류", Toast.LENGTH_SHORT).show()
                            return
                        }

                        // JSON 응답을 ClusterData 객체로 변환
                        val clusterDataList = clusterMap.map { (categoryName, appList) ->
                            ClusterData(
                                category_name = categoryName,
                                apps = appList.mapNotNull { app ->
                                    val appId = app["App ID"] as? String
                                    val iconUrl = app["Icon URL"] as? String
                                    val name = app["Name"] as? String

                                    if (appId != null && iconUrl != null && name != null) {
                                        AppData(appId = appId, iconUrl = iconUrl, name = name)
                                    } else {
                                        Log.e("API_RESPONSE", "Missing fields in app data: $app")
                                        null  // 필수 필드가 누락된 경우 해당 항목을 생략
                                    }
                                }
                            )
                        }

                        // RecyclerView에 설정
                        val clusterAdapter = ClusterAdapter(clusterDataList)
                        recyclerView.adapter = clusterAdapter
                    } else {
                        Log.e(
                            "API_RESPONSE",
                            "Response failed with status code: ${response.code()}"
                        )
                        Toast.makeText(this@FunctionActivity, "클러스터링 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ClusterResponse>, t: Throwable) {
                    Log.e("API_RESPONSE", "Failed to connect to server: ${t.message}")
                    Toast.makeText(this@FunctionActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}