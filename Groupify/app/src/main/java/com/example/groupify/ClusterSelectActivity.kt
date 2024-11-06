package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ClusterSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster_select)

        // 버튼 클릭 이벤트 설정
        findViewById<Button>(R.id.btnCluster4).setOnClickListener { startClusterLauncher(4) }
        findViewById<Button>(R.id.btnCluster5).setOnClickListener { startClusterLauncher(5) }
        findViewById<Button>(R.id.btnCluster6).setOnClickListener { startClusterLauncher(6) }
        findViewById<Button>(R.id.btnCluster7).setOnClickListener { startClusterLauncher(7) }
        findViewById<Button>(R.id.btnCluster8).setOnClickListener { startClusterLauncher(8) }
        findViewById<Button>(R.id.btnCluster9).setOnClickListener { startClusterLauncher(9) }
        findViewById<Button>(R.id.btnCluster10).setOnClickListener { startClusterLauncher(10) }
        findViewById<Button>(R.id.btnCluster11).setOnClickListener { startClusterLauncher(11) }
        findViewById<Button>(R.id.btnCluster12).setOnClickListener { startClusterLauncher(12) }
    }

    // 새로운 액티비티로 클러스터 개수를 전달하면서 시작
    private fun startClusterLauncher(clusterCount: Int) {
//        val intent = Intent(this, ClusterLauncherActivity::class.java)
        intent.putExtra("CLUSTER_COUNT", clusterCount) // 클러스터 수 전달
        startActivity(intent) // 새 액티비티 실행
    }
}
