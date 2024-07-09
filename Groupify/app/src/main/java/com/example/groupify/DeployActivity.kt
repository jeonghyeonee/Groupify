package com.example.groupify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeployActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deploy)

        val button4x5 = findViewById<Button>(R.id.button_4x5)
        val button5x6 = findViewById<Button>(R.id.button_5x6)
        val button6x6 = findViewById<Button>(R.id.button_6x6)
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        button4x5.setOnClickListener {
            createGrid(gridLayout, 4, 5)
        }

        button5x6.setOnClickListener {
            createGrid(gridLayout, 5, 6)
        }

        button6x6.setOnClickListener {
            createGrid(gridLayout, 6, 6)
        }

        val buttonNext = findViewById<Button>(R.id.button_next)
        buttonNext.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createGrid(gridLayout: GridLayout, columns: Int, rows: Int) {
        gridLayout.removeAllViews()
        gridLayout.columnCount = columns
        gridLayout.rowCount = rows

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val textView = TextView(this).apply {

                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(android.R.drawable.btn_default)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = GridLayout.LayoutParams.WRAP_CONTENT
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        setMargins(8, 8, 8, 8)
                    }
                }
                gridLayout.addView(textView)
            }
        }
    }
}
