package com.example.groupify

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.palette.graphics.Palette
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ColorClassify(private val context: Context) {

    fun classifyApps(appContainer: LinearLayout) {
        try {
            // PackageManager를 사용하여 설치된 앱 리스트를 가져옴
            val packageManager: PackageManager = context.packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            for (packageInfo in packages) {
                // 구글 플레이 스토어에서 설치된 앱만 필터링
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                    // 로그 출력
                    Log.d("AppInfo", "App Name: $appName")

                    // 동적으로 텍스트뷰와 이미지뷰 생성
                    val textView = TextView(context)
                    textView.text = appName

                    val imageView = ImageView(context)
                    imageView.setImageDrawable(appIcon)
                    imageView.layoutParams = LinearLayout.LayoutParams(100, 100) // 아이콘 크기 설정

                    // 컨테이너에 추가
                    val appLayout = LinearLayout(context)
                    appLayout.orientation = LinearLayout.HORIZONTAL
                    appLayout.addView(imageView)
                    appLayout.addView(textView)

                    appContainer.addView(appLayout)
                }
            }
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
