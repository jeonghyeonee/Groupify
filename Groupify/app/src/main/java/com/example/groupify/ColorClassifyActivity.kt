package com.example.groupify

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.groupify.ml.KMeans
import kotlin.math.sqrt

class ColorClassifyActivity : AppCompatActivity() {

    private lateinit var selectedColors: List<Pair<String, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_classify)

        try {
            val colorCount = intent.getIntExtra("colorCount", 5)
            Log.d("ColorClassifyActivity", "Received color count: $colorCount")

            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val appContainer: LinearLayout = findViewById(R.id.appContainer)

            val colors = mutableListOf<Int>()
            val appInfos = mutableListOf<Pair<String, Int>>()

            for (packageInfo in packages) {
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                    val dominantColor = getDominantColor(appIcon)
                    colors.add(dominantColor)
                    appInfos.add(Pair(appName, dominantColor))
                }
            }

            val kmeans = KMeans(colorCount)
            val clusters = kmeans.fit(colors.toIntArray())

            val colorGroups = clusters.withIndex().groupBy { it.value }.mapValues { entry ->
                entry.value.map { appInfos[it.index] }
            }

            for ((groupId, apps) in colorGroups) {
                val groupTextView = TextView(this).apply {
                    text = "Group $groupId"
                    setPadding(10, 10, 10, 10)
                    setBackgroundColor(apps.first().second)
                    setTextColor(0xFFFFFFFF.toInt())
                }
                appContainer.addView(groupTextView)

                for ((appName, appColor) in apps) {
                    val appLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(20, 5, 20, 5)
                    }

                    val appTextView = TextView(this).apply {
                        text = appName
                        setPadding(10, 0, 10, 0)
                    }

                    val colorImageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(50, 50)
                        setBackgroundColor(appColor)
                    }

                    appLayout.addView(appTextView)
                    appLayout.addView(colorImageView)
                    appContainer.addView(appLayout)
                }
            }
        } catch (e: Exception) {
            Log.e("ColorClassifyActivity", "Error retrieving app information", e)
        }
    }

    private fun getDominantColor(drawable: Drawable): Int {
        val bitmap = drawableToBitmap(drawable)
        val palette = Palette.from(bitmap).generate()
        return palette.getDominantColor(0x000000)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is AdaptiveIconDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}
