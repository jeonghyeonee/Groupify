package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

//k-means에 사용
class ColorRangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_range)

        val editText = findViewById<EditText>(R.id.editTextColorCount)
        val buttonConfirm = findViewById<Button>(R.id.buttonConfirm)

        buttonConfirm.setOnClickListener {
            try {
                val colorCount = editText.text.toString().toIntOrNull()
                if (colorCount != null && colorCount > 0) {
                    val intent = Intent(this, ColorClassifyActivity::class.java).apply {
                        putExtra("colorCount", colorCount)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please enter a valid number of colors", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
