package com.example.groupify

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ColorName : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup
    private lateinit var confirmButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_color)

        radioGroup = findViewById(R.id.radioGroup)
        confirmButton = findViewById(R.id.confirmButton)
        resultTextView = findViewById(R.id.resultTextView)

        confirmButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedType = when (selectedId) {
                R.id.radioColorName -> "색깔 이름"
                R.id.radioHeart -> "하트"
                R.id.radioIcon -> "아이콘"
                else -> ""
            }

            resultTextView.text = "선택한 클러스터 이름 유형: $selectedType"
        }
    }
}
