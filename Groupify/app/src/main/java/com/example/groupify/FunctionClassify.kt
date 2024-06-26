package com.example.groupify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class FunctionClassify : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.classify_function)

        // '기능분류 화면입니다' 텍스트를 설정
        val textView: TextView = findViewById(R.id.textViewFunctionClassify)
        textView.text = "기능분류 화면입니다"
    }
}
