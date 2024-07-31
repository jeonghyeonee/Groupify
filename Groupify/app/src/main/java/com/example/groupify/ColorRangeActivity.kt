//package com.example.groupify
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.LinearLayout
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileInputStream
//import java.io.IOException
//import java.io.ObjectInputStream
//import kotlin.math.sqrt
//import kotlin.random.Random
//
//class ColorRangeActivity : AppCompatActivity() {
//    private lateinit var linearLayout: LinearLayout
//    private lateinit var editText: EditText
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_color_range)
//
//        linearLayout = findViewById(R.id.linearLayout)
//        editText = findViewById(R.id.editText)
//
//        val buttonCluster = findViewById<Button>(R.id.button_cluster)
//        buttonCluster.setOnClickListener {
//            val k = editText.text.toString().toIntOrNull()
//            if (k != null && k > 0) {
//                Log.d("ColorRangeActivity", "클러스터링 버튼 클릭됨, K: $k")
//                performClustering(k)
//            } else {
//                Log.e("ColorRangeActivity", "유효한 클러스터 수 (K)를 입력하세요.")
//            }
//        }
//    }
//
//    private fun performClustering(k: Int) {
//        CoroutineScope(Dispatchers.IO).launch {
//            Log.d("ColorRangeActivity", "performClustering 시작")
//            try {
//                val dataFile = File(getExternalFilesDir(null), "AppData.dat")
//                if (!dataFile.exists()) {
//                    Log.e("ColorRangeActivity", "AppData 파일을 찾을 수 없음")
//                    return@launch
//                }
//
//                val appDataList: List<AppData>
//                try {
//                    ObjectInputStream(FileInputStream(dataFile)).use { input ->
//                        @Suppress("UNCHECKED_CAST")
//                        appDataList = input.readObject() as List<AppData>
//                    }
//                } catch (e: IOException) {
//                    Log.e("ColorRangeActivity", "Error reading AppData", e)
//                    return@launch
//                } catch (e: ClassNotFoundException) {
//                    Log.e("ColorRangeActivity", "Class not found when reading AppData", e)
//                    return@launch
//                }
//
//                val allColors = appDataList.map { it.dominantColors }
//                if (allColors.isEmpty()) {
//                    Log.e("ColorRangeActivity", "No colors found in AppData")
//                    return@launch
//                }
//
//                val clusters = kMeansClustering(allColors, k)
//                Log.d("ColorRangeActivity", "클러스터링 완료, 클러스터 수: ${clusters.size}")
//
//                withContext(Dispatchers.Main) {
//                    linearLayout.removeAllViews()
//                    clusters.forEachIndexed { clusterIndex, cluster ->
//                        val clusterLayout = LinearLayout(this@ColorRangeActivity).apply {
//                            orientation = LinearLayout.VERTICAL
//                            setPadding(16, 16, 16, 16)
//                        }
//
//                        val clusterTitle = TextView(this@ColorRangeActivity).apply {
//                            text = "Cluster ${clusterIndex + 1}"
//                            textSize = 18f
//                            setTextColor(Color.BLACK)
//                        }
//
//                        clusterLayout.addView(clusterTitle)
//
//                        cluster.forEach { color ->
//                            val matchingApps = appDataList.filter {
//                                it.dominantColors.contentEquals(color)
//                            }
//                            matchingApps.forEach { appData ->
//                                val appLayout = LinearLayout(this@ColorRangeActivity).apply {
//                                    orientation = LinearLayout.HORIZONTAL
//                                    setPadding(8, 8, 8, 8)
//                                }
//
//                                val appIcon = ImageView(this@ColorRangeActivity).apply {
//                                    val iconFile = File(getExternalFilesDir(null), "AppIcons/${appData.iconFileName}")
//                                    if (iconFile.exists()) {
//                                        setImageBitmap(BitmapFactory.decodeFile(iconFile.absolutePath))
//                                    }
//                                    layoutParams = LinearLayout.LayoutParams(100, 100)
//                                }
//
//                                val appName = TextView(this@ColorRangeActivity).apply {
//                                    text = appData.name
//                                    textSize = 16f
//                                    setTextColor(Color.BLACK)
//                                }
//
//                                appLayout.addView(appIcon)
//                                appLayout.addView(appName)
//                                clusterLayout.addView(appLayout)
//                            }
//                        }
//
//                        linearLayout.addView(clusterLayout)
//                    }
//                    Log.d("ColorRangeActivity", "결과 UI에 반영됨")
//                }
//            } catch (e: Exception) {
//                Log.e("ColorRangeActivity", "Exception in performClustering", e)
//            }
//        }
//    }
//
//    private fun kMeansClustering(colors: List<IntArray>, k: Int): List<List<IntArray>> {
//        val centers = mutableListOf<IntArray>()
//        val random = Random(System.currentTimeMillis())
//        for (i in 0 until k) {
//            centers.add(colors[random.nextInt(colors.size)])
//        }
//
//        var clusters = Array(k) { mutableListOf<IntArray>() }
//        var lastCenters: List<IntArray>
//
//        do {
//            clusters.forEach { it.clear() }
//            for (color in colors) {
//                val closestCenter = centers.minByOrNull { center -> colorDistance(center, color) }!!
//                clusters[centers.indexOf(closestCenter)].add(color)
//            }
//
//            lastCenters = centers.map { it.copyOf() }
//            centers.forEachIndexed { index, center ->
//                if (clusters[index].isNotEmpty()) {
//                    val mean = clusters[index].reduce { acc, color ->
//                        intArrayOf(
//                            acc[0] + color[0],
//                            acc[1] + color[1],
//                            acc[2] + color[2]
//                        )
//                    }.map { it / clusters[index].size }.toIntArray()
//                    centers[index] = mean
//                }
//            }
//        } while (!centers.zip(lastCenters).all { (a, b) -> a.contentEquals(b) })
//
//        return clusters.toList()
//    }
//
//    private fun colorDistance(c1: IntArray, c2: IntArray): Double {
//        return sqrt(((c1[0] - c2[0]) * (c1[0] - c2[0]) +
//                (c1[1] - c2[1]) * (c1[1] - c2[1]) +
//                (c1[2] - c2[2]) * (c1[2] - c2[2])).toDouble())
//    }
//}
