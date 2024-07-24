package com.example.groupify

import android.graphics.Color
import android.os.Bundle
import android.widget.GridLayout
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

            val gridLayout = GridLayout(this).apply {
                rowCount = 3 // 적절한 행 수로 설정
                columnCount = 3 // 적절한 열 수로 설정
                setPadding(10, 10, 10, 10)
            }

            for ((appName, appColor) in apps) {
                val appLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 5, 20, 5)
                }

                val colorView = TextView(this).apply {
                    setBackgroundColor(appColor)
                    text = ""
                    layoutParams = LinearLayout.LayoutParams(100, 100)
                }

                val appTextView = TextView(this).apply {
                    text = appName
                    textSize = 14f
                    setPadding(10, 5, 10, 5)
                    maxLines = 2 // 필요한 경우 줄 수 조정
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                appLayout.addView(colorView)
                appLayout.addView(appTextView)
                gridLayout.addView(appLayout)
            }

            groupLayout.addView(gridLayout)
            folderContainer.addView(groupLayout)
        }
    }
}
