package com.example.groupify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ColorSelectionActivity : AppCompatActivity() {

    private val colors = listOf(
        "핑크" to Color.parseColor("#FFC0CB"),
        "빨강" to Color.parseColor("#FF0000"),
        "주황" to Color.parseColor("#FFA500"),
        "노랑" to Color.parseColor("#FFFF00"),
        "초록" to Color.parseColor("#008000"),
        "하늘색" to Color.parseColor("#87CEEB"),
        "남색" to Color.parseColor("#000080"),
        "보라색" to Color.parseColor("#800080"),
        "갈색" to Color.parseColor("#A52A2A"),
        "검정" to Color.parseColor("#000000"),
        "흰색" to Color.parseColor("#FFFFFF")
    )

    private val selectedColors = mutableListOf<Pair<String, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_selection)

        val gridLayout = findViewById<GridLayout>(R.id.colorGrid)
        gridLayout.rowCount = 4
        gridLayout.columnCount = 3

        colors.forEach { (name, color) ->
            val colorButton = Button(this).apply {
                text = name
                setBackgroundColor(color)
                setOnClickListener {
                    if (selectedColors.contains(name to color)) {
                        selectedColors.remove(name to color)
                        setBackgroundColor(color)
                    } else {
                        selectedColors.add(name to color)
                        setBackgroundResource(R.drawable.selected_button_background)
                    }
                }
            }
            gridLayout.addView(colorButton)
        }

        val confirmButton = findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            if (selectedColors.size >= 2) {
                val intent = Intent(this, ColorClassify::class.java).apply {
                    putExtra("selectedColors", ArrayList(selectedColors))
                }
                startActivity(intent)
            } else {
                // 그룹 개수는 최소 2개 이상이어야 합니다.
                val warningTextView = findViewById<TextView>(R.id.warningTextView)
                warningTextView.text = "그룹 개수는 최소 2개 이상이어야 합니다."
            }
        }
    }
}