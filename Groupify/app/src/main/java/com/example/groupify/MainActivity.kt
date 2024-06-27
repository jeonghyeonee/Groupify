package com.example.groupify

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // PackageManager를 사용하여 설치된 앱 리스트를 가져옴
            val packageManager: PackageManager = packageManager
            val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            // 앱 이름과 아이콘을 표시할 컨테이너
            val appContainer: LinearLayout = findViewById(R.id.appContainer)

            for (packageInfo in packages) {
                // 구글 플레이 스토어에서 설치된 앱만 필터링
                if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                    // 로그 출력
                    Log.d("AppInfo", "App Name: $appName")

                    // 동적으로 텍스트뷰와 이미지뷰 생성
                    val textView = TextView(this)
                    textView.text = appName

                    val imageView = ImageView(this)
                    imageView.setImageDrawable(appIcon)
                    imageView.layoutParams = LinearLayout.LayoutParams(100, 100) // 아이콘 크기 설정

                    // 컨테이너에 추가
                    val appLayout = LinearLayout(this)
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
}
