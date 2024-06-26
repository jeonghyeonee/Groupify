package com.example.groupify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout

class SelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val buttonColor = findViewById<Button>(R.id.button_color)
        buttonColor.setOnClickListener {
            val appContainer: LinearLayout = findViewById(R.id.appContainer)
            val colorClassify = ColorClassify(this)
            colorClassify.classifyApps(appContainer)
        }

        // 기능 분류 버튼 클릭 리스너 추가
        val buttonFunction = findViewById<Button>(R.id.button_function)
        buttonFunction.setOnClickListener {
            val intent = Intent(this, FunctionClassify::class.java)
            startActivity(intent)
        }
    }
}
