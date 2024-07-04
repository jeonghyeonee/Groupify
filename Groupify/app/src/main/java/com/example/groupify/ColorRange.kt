package com.example.groupify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
import android.widget.TextView
import android.graphics.Color
import android.view.View

class ColorRange : AppCompatActivity() {
    private lateinit var colorPreview: View
    private lateinit var seekBars: Array<SeekBar>
    private lateinit var colorTextViews: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_range)

        colorPreview = findViewById(R.id.color_preview)

        // SeekBar와 TextView 배열 초기화
        seekBars = arrayOf(
            findViewById(R.id.seekbar1),
            findViewById(R.id.seekbar2),
            findViewById(R.id.seekbar3),
            findViewById(R.id.seekbar4),
            findViewById(R.id.seekbar5)
        )

        colorTextViews = arrayOf(
            findViewById(R.id.color_text_view1),
            findViewById(R.id.color_text_view2),
            findViewById(R.id.color_text_view3),
            findViewById(R.id.color_text_view4),
            findViewById(R.id.color_text_view5),
            findViewById(R.id.color_text_view6)
        )

        // 색상 범위 설정
        val colorRanges = arrayOf(
            Pair(Color.parseColor("#FF0000"), Color.parseColor("#FF7F00")), // 빨강 -> 주황
            Pair(Color.parseColor("#FF7F01"), Color.parseColor("#FFFF00")), // 주황 -> 노랑
            Pair(Color.parseColor("#FFFF01"), Color.parseColor("#00FF00")), // 노랑 -> 초록
            Pair(Color.parseColor("#00FF01"), Color.parseColor("#0000FF")), // 초록 -> 파랑
            Pair(Color.parseColor("#0000FF"), Color.parseColor("#7F00FF")), // 파랑 -> 보라
            Pair(Color.parseColor("#7F00FF"), Color.parseColor("#000000"))  // 보라 -> 검정
        )

        // 각 SeekBar의 최대값 설정 및 초기값 설정
        for ((index, seekBar) in seekBars.withIndex()) {
            val (startColor, endColor) = colorRanges[index]
            seekBar.max = endColor - startColor
            seekBar.progress = 0 // 초기값 설정
            updateColorPreview(index, startColor, endColor)

            // SeekBar 변경 리스너 설정
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateColorPreview(index, startColor, endColor)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }
            })
        }
    }

    private fun updateColorPreview(index: Int, startColor: Int, endColor: Int) {
        val progress = seekBars[index].progress
        val color = interpolateColor(startColor, endColor, progress.toFloat() / seekBars[index].max)
        val hexColor = String.format("#%06X", 0xFFFFFF and color)

        colorPreview.setBackgroundColor(color)
        colorTextViews[index].text = "Boundary ${index + 1}: $hexColor"
        colorTextViews[index].setBackgroundColor(color)
    }

    private fun interpolateColor(startColor: Int, endColor: Int, factor: Float): Int {
        val inverseFactor = 1 - factor
        val a = Color.alpha(startColor) * inverseFactor + Color.alpha(endColor) * factor
        val r = Color.red(startColor) * inverseFactor + Color.red(endColor) * factor
        val g = Color.green(startColor) * inverseFactor + Color.green(endColor) * factor
        val b = Color.blue(startColor) * inverseFactor + Color.blue(endColor) * factor
        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }
}
