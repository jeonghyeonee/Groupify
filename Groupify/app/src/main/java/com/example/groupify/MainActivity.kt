package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val logoImage2 = findViewById<ImageView>(R.id.logoImage2)

        // 처음에는 보이지 않도록 설정
        logoImage.visibility = View.INVISIBLE
        logoImage2.visibility = View.INVISIBLE

        // 2.5초 후 이미지들이 나타나도록 설정
        Handler(Looper.getMainLooper()).postDelayed({
            // 이미지를 visible로 설정
            logoImage.visibility = View.VISIBLE
            logoImage2.visibility = View.VISIBLE

            // 아래에서 위로 이동하며 서서히 나타나는 애니메이션 설정
            val translate = TranslateAnimation(0f, 0f, 300f, 0f) // 아래에서 위로 300px 이동
            translate.duration = 1500

            val fadeIn = AlphaAnimation(0f, 1f) // 서서히 나타남
            fadeIn.duration = 1500

            // 애니메이션을 함께 적용
            val animationSet1 = AnimationSet(true)
            animationSet1.addAnimation(translate)
            animationSet1.addAnimation(fadeIn)
            logoImage.startAnimation(animationSet1)

            val animationSet2 = AnimationSet(true)
            animationSet2.addAnimation(translate)
            animationSet2.addAnimation(fadeIn)
            logoImage2.startAnimation(animationSet2)

            // 애니메이션 후 2초 대기 후 다른 화면으로 이동
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish() // MainActivity 종료
            }, 2000) // 애니메이션 후 2초 대기

        }, 2500) // 처음 2.5초 대기 후 애니메이션 실행
    }
}
