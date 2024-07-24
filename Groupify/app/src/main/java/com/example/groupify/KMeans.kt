package com.example.groupify.ml

import kotlin.math.sqrt

class KMeans(private val k: Int) {
    fun fit(data: IntArray): IntArray {
        val centroids = data.copyOfRange(0, k).toMutableList()
        var clusters = IntArray(data.size)
        var hasChanged = true

        while (hasChanged) {
            hasChanged = false

            // 할당
            for (i in data.indices) {
                val nearestCentroid = centroids.indices.minByOrNull { j -> colorDistance(data[i], centroids[j]) }!!
                if (clusters[i] != nearestCentroid) {
                    clusters[i] = nearestCentroid
                    hasChanged = true
                }
            }

            // 중심점 업데이트
            val newCentroids = MutableList(k) { 0 }
            val counts = MutableList(k) { 0 }

            for (i in data.indices) {
                newCentroids[clusters[i]] += data[i]
                counts[clusters[i]]++
            }

            for (j in centroids.indices) {
                if (counts[j] > 0) {
                    centroids[j] = newCentroids[j] / counts[j]
                }
            }
        }

        return clusters
    }

    private fun colorDistance(color1: Int, color2: Int): Double {
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF

        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF

        return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toDouble())
    }
}
