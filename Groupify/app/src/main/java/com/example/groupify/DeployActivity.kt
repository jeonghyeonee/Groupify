package com.example.groupify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class DeployActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deploy)

        val button4x5 = findViewById<Button>(R.id.button_4x5)
        val button5x6 = findViewById<Button>(R.id.button_5x6)
        val button6x6 = findViewById<Button>(R.id.button_6x6)

        button4x5.setOnClickListener {
            // 4x5 버튼 클릭 시 동작 추가
        }

        button5x6.setOnClickListener {
            // 5x6 버튼 클릭 시 동작 추가
        }

        button6x6.setOnClickListener {
            // 6x6 버튼 클릭 시 동작 추가
        }

        val buttonNext = findViewById<Button>(R.id.button_next)
        buttonNext.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
        }
    }
}
