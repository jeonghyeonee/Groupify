package com.example.groupify

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groupify.R
import com.example.groupify.SelectionActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val joinNowButton = findViewById<Button>(R.id.joinNowButton)
        val welcomeText1 = findViewById<TextView>(R.id.welcomeText1)
        val welcomeText2 = findViewById<TextView>(R.id.welcomeText2)



        // HTML 형식의 텍스트 설정
        welcomeText1.text = getString(R.string.welcome_message_1)
        welcomeText2.text = Html.fromHtml(getString(R.string.welcome_message_2), Html.FROM_HTML_MODE_LEGACY)

        joinNowButton.background = ContextCompat.getDrawable(this,R.drawable.border_radius)
        joinNowButton.setBackgroundColor(Color.parseColor("#4b5ae4"))

        // 3초 후 버튼과 스크롤 애니메이션 동시에 시작
        Handler(Looper.getMainLooper()).postDelayed({
            // 버튼이 아래에서 위로 올라오는 애니메이션
            joinNowButton.visibility = View.VISIBLE
            val buttonAnimation = ObjectAnimator.ofFloat(joinNowButton, "translationY", 300f, 0f)

            // ScrollView를 스크롤하는 애니메이션
            val scrollAnimation = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.bottom)

            // 애니메이션 동시 실행 및 설정
            buttonAnimation.duration = 1000
            scrollAnimation.duration = 1000

            // 두 애니메이션 동시에 시작
            buttonAnimation.start()
            scrollAnimation.start()
        }, 3000)

        // Join Now 버튼 클릭 시 SelectionActivity로 이동
        joinNowButton.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
