package com.example.groupify

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette

class ColorClassify : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classify_color)

        val colorCodes = arrayListOf<Int>()

        try {
            // PackageManager를 사용하여 설치된 앱 리스트를 가져옴
            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            // 앱 정보를 표시할 LinearLayout
            val appContainer: LinearLayout = findViewById(R.id.appContainer)

            for (packageInfo in packages) {
                // 구글 플레이 스토어에서 설치된 앱만 필터링
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                    // 아이콘의 대표 색상 추출
                    val dominantColor = getDominantColor(appIcon)
                    colorCodes.add(dominantColor)

                    // 앱 이름과 대표 색상 코드를 표시할 TextView 생성
                    val textView = TextView(this)
                    textView.text = "$appName - Dominant Color: #${Integer.toHexString(dominantColor)}"
                    textView.setPadding(10, 10, 10, 10)

                    // TextView를 LinearLayout에 추가
                    appContainer.addView(textView)

                    // 로그 출력
                    Log.d("AppInfo", "App Name: $appName, Dominant Color: #${Integer.toHexString(dominantColor)}")
                }
            }

            // ColorRange 액티비티로 대표 색상 코드 전달
            val intent = Intent(this, ColorRange::class.java)
            intent.putIntegerArrayListExtra("colorCodes", colorCodes)
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("AppInfo", "Error retrieving app information", e)
        }
    }

    private fun getDominantColor(drawable: Drawable): Int {
        val bitmap = drawableToBitmap(drawable)
        val palette = Palette.from(bitmap).generate()
        return palette.getDominantColor(0x000000) // 기본값으로 검정색 반환
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
}
