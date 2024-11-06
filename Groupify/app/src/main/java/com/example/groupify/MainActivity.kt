package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.groupify.R
import com.example.groupify.SelectionActivity
import com.example.groupify.WelcomeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val logoImage = findViewById<ImageView>(R.id.logoImage)

        // 처음에는 logoImage를 보이지 않게 설정
        logoImage.visibility = View.INVISIBLE

        // 3초 후 logoImage가 나타나도록 설정
        Handler(Looper.getMainLooper()).postDelayed({
            // logoImage를 visible로 설정
            logoImage.visibility = View.VISIBLE

            // 왼쪽에서 오른쪽으로 이동하면서 나타나는 애니메이션 설정
            val translate = TranslateAnimation(-logoImage.width.toFloat(), 0f, 0f, 0f)
            translate.duration = 1000

            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.duration = 1000

            // 애니메이션을 함께 적용
            val animationSet = AnimationSet(true)
            animationSet.addAnimation(translate)
            animationSet.addAnimation(fadeIn)
            logoImage.startAnimation(animationSet)

            // logoImage가 나타난 3초 후 SelectionActivity로 이동
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish() // MainActivity 종료
            }, 2000) // logoImage가 나타난 후 3초 대기

        }, 2500) // 처음에 3초 대기 후 logoImage 표시
    }
}