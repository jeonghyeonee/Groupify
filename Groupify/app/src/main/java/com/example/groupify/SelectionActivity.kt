package com.example.groupify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val buttonColor = findViewById<Button>(R.id.button_color)
        buttonColor.setOnClickListener {
            val intent = Intent(this, ColorSelectionActivity::class.java)
            startActivity(intent)
        }

        val buttonFunction = findViewById<Button>(R.id.button_function)
        buttonFunction.setOnClickListener {
            val intent = Intent(this, FunctionClassify::class.java)
            startActivity(intent)
        }

        val buttonColorRange = findViewById<Button>(R.id.button_color_range)
        buttonColorRange.setOnClickListener {
                val intent = Intent(this, ColorRangeActivity::class.java)
            startActivity(intent)
        }
    }
}
