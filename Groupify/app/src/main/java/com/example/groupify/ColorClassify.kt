package com.example.groupify

import android.content.Intent
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
import kotlin.math.sqrt

class ColorClassify : AppCompatActivity() {

    private lateinit var selectedColors: List<Pair<String, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classify_color)

        selectedColors = intent.getSerializableExtra("selectedColors") as List<Pair<String, Int>>

        val colorGroups = HashMap<String, MutableList<Pair<String, Int>>>()

        try {
            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val appContainer: LinearLayout = findViewById(R.id.appContainer)

            for (packageInfo in packages) {
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                    val dominantColor = getDominantColor(appIcon)
                    val groupName = getClosestColorGroup(dominantColor, selectedColors)

                    if (colorGroups.containsKey(groupName)) {
                        colorGroups[groupName]?.add(Pair(appName, dominantColor))
                    } else {
                        colorGroups[groupName] = mutableListOf(Pair(appName, dominantColor))
                    }
                }
            }

            // 그룹별 대표 색상과 앱 이름 표시
            for ((groupName, apps) in colorGroups) {
                val groupColor = selectedColors.find { it.first == groupName }?.second ?: Color.BLACK
                val hexColor = String.format("#%06X", 0xFFFFFF and groupColor)

                val groupTextView = TextView(this)
                groupTextView.text = "$groupName - Representative Color: $hexColor"
                groupTextView.setPadding(10, 10, 10, 10)
                groupTextView.setBackgroundColor(groupColor)
                groupTextView.setTextColor(Color.WHITE)
                appContainer.addView(groupTextView)

                for ((appName, appColor) in apps) {
                    val appLayout = LinearLayout(this)
                    appLayout.orientation = LinearLayout.HORIZONTAL
                    appLayout.setPadding(20, 5, 20, 5)

                    val appTextView = TextView(this)
                    appTextView.text = "$appName (Group: $groupName)"
                    appTextView.setPadding(10, 0, 10, 0)

                    val colorImageView = ImageView(this)
                    colorImageView.layoutParams = LinearLayout.LayoutParams(50, 50)
                    colorImageView.setBackgroundColor(appColor)

                    appLayout.addView(appTextView)
                    appLayout.addView(colorImageView)
                    appContainer.addView(appLayout)
                }
            }

            // Intent to start FolderViewActivity with colorGroups
            val intent = Intent(this, FolderViewActivity::class.java).apply {
                putExtra("colorGroups", HashMap(colorGroups.mapValues { ArrayList(it.value) }))
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("AppInfo", "Error retrieving app information", e)
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
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun getClosestColorGroup(color: Int, groupColors: List<Pair<String, Int>>): String {
        var closestGroup = "Others"
        var minDistance = Double.MAX_VALUE

        for ((groupName, groupColor) in groupColors) {
            val distance = colorDistance(color, groupColor)
            if (distance < minDistance) {
                minDistance = distance
                closestGroup = groupName
            }
        }

        return closestGroup
    }

    private fun colorDistance(color1: Int, color2: Int): Double {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toDouble())
    }
}
