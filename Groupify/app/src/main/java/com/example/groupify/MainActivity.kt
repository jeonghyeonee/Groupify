package com.example.groupify

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.palette.graphics.Palette
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // PackageManager를 사용하여 설치된 앱 리스트를 가져옴
            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            // 앱 이름을 저장할 리스트
            val appNames = arrayListOf<String>()

            for (packageInfo in packages) {
                // 구글 플레이 스토어에서 설치된 앱만 필터링
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                    // 아이콘의 대표 색상 추출
                    val dominantColor = getDominantColor(appIcon)

                    appNames.add("$appName - Dominant Color: #${Integer.toHexString(dominantColor)}")
                    Log.d("AppInfo", "App Name: $appName, Dominant Color: #${Integer.toHexString(dominantColor)}")
                }
            }

            // 예: TextView에 출력하기 위한 코드
            val textView: TextView = findViewById(R.id.textView)
            textView.text = appNames.joinToString("\n")
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
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is AdaptiveIconDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } else {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
