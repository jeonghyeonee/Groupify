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

    // 앱 아이콘의 대표 색상 코드 추출
    private fun getDominantColorHex(drawable: Drawable): String {
        val bitmap = drawableToBitmap(drawable)

        // 색상 빈도를 저장할 맵 초기화
        val colorFrequencyMap: MutableMap<Int, Int> = HashMap()

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixelColor = bitmap.getPixel(x, y)

                // 완전히 투명한 픽셀 무시
                if (Color.alpha(pixelColor) < 255) continue

                // 거의 흰색이나 거의 검은색은 무시
                if (isNearWhiteOrBlack(pixelColor)) continue

                val colorCount = colorFrequencyMap[pixelColor] ?: 0
                colorFrequencyMap[pixelColor] = colorCount + 1
            }
        }

        // 가장 빈도가 높은 색상 선택
        val dominantColor = colorFrequencyMap.maxByOrNull { it.value }?.key ?: Color.GRAY

        // 헥사 코드로 변환
        return String.format("#%06X", 0xFFFFFF and dominantColor)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isNearWhiteOrBlack(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        // 흰색이나 검은색 근처의 색상을 무시 (범위를 조정 가능)
        val nearWhiteThreshold = 245
        val nearBlackThreshold = 10
        return (red > nearWhiteThreshold && green > nearWhiteThreshold && blue > nearWhiteThreshold) ||
                (red < nearBlackThreshold && green < nearBlackThreshold && blue < nearBlackThreshold)
    }

}
