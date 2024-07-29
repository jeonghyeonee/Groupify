package com.example.groupify

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.groupify.ml.KMeans
import kotlin.math.sqrt

class ColorClassifyActivity : AppCompatActivity() {

    private lateinit var colorGroups: HashMap<Int, ArrayList<Triple<String, Bitmap, Int>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_view) // activity_color_classify 대신 activity_folder_view 사용

        try {
            val colorCount = intent.getIntExtra("colorCount", 5)
            Log.d("ColorClassifyActivity", "Received color count: $colorCount")

            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val folderContainer: GridLayout = findViewById(R.id.folderContainer)

            val colors = mutableListOf<Int>()
            val appInfos = mutableListOf<Triple<String, Bitmap, Int>>()

            for (packageInfo in packages) {
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                    val dominantColor = getDominantColor(appIcon)
                    val appIconBitmap = drawableToBitmap(appIcon)
                    colors.add(dominantColor)
                    appInfos.add(Triple(appName, appIconBitmap, dominantColor))
                }
            }

            val kmeans = KMeans(colorCount)
            val clusters = kmeans.fit(colors.toIntArray())

            colorGroups = clusters.withIndex().groupBy { it.value }.mapValues { entry ->
                entry.value.map { appInfos[it.index] }.toCollection(ArrayList())
            } as HashMap<Int, ArrayList<Triple<String, Bitmap, Int>>>

            for ((groupId, apps) in colorGroups) {
                val groupColor = apps.first().third

                val groupLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 10, 20, 10)
                    setBackgroundColor(groupColor)
                }

                val groupTitle = TextView(this).apply {
                    text = "Group $groupId"
                    textSize = 18f
                    setPadding(10, 10, 10, 10)
                    setBackgroundColor(groupColor)
                    setTextColor(0xFFFFFFFF.toInt())
                }

                groupLayout.addView(groupTitle)

                val gridLayout = GridLayout(this).apply {
                    rowCount = 3
                    columnCount = 3
                    setPadding(10, 10, 10, 10)
                }

                for ((appName, appIconBitmap, appColor) in apps) {
                    val appLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(20, 5, 20, 5)
                    }

                    val appIconView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(100, 100)
                        setImageBitmap(appIconBitmap)
                    }

                    val appTextView = TextView(this).apply {
                        text = appName
                        textSize = 14f
                        setPadding(10, 5, 10, 5)
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    appLayout.addView(appIconView)
                    appLayout.addView(appTextView)
                    gridLayout.addView(appLayout)
                }

                groupLayout.addView(gridLayout)
                folderContainer.addView(groupLayout)
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
