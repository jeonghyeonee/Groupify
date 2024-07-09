package com.example.groupify

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FolderViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_view)

        val colorGroups = intent.getSerializableExtra("colorGroups") as HashMap<String, ArrayList<Pair<String, Int>>>
        val folderContainer: LinearLayout = findViewById(R.id.folderContainer)

        for ((groupName, apps) in colorGroups) {
            val groupLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 10, 20, 10)
                setBackgroundColor(Color.LTGRAY)
            }

            val groupTitle = TextView(this).apply {
                text = groupName
                textSize = 18f
                setPadding(10, 10, 10, 10)
                setBackgroundColor(Color.DKGRAY)
                setTextColor(Color.WHITE)
            }

            groupLayout.addView(groupTitle)

            for ((appName, appColor) in apps) {
                val appTextView = TextView(this).apply {
                    text = appName
                    textSize = 16f
                    setPadding(10, 5, 10, 5)
                    setBackgroundColor(appColor)
                }
                groupLayout.addView(appTextView)
            }

            folderContainer.addView(groupLayout)
        }
    }
}
