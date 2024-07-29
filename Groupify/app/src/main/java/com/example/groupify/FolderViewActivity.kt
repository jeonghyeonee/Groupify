package com.example.groupify

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FolderViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_view)

        val colorGroups = intent.getSerializableExtra("colorGroups") as HashMap<Int, ArrayList<Triple<String, Bitmap, Int>>>
        val folderContainer: GridLayout = findViewById(R.id.folderContainer)

        for ((groupId, apps) in colorGroups) {
            val groupColor = apps.first().third

            val groupLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 10, 20, 10)
                setBackgroundColor(groupColor)
            }

            val groupTitle = TextView(this).apply {
                text = "Group $groupId"
                textSize = 18f
                setPadding(10, 10, 10, 10)
                setBackgroundColor(groupColor)
                setTextColor(0xFFFFFFFF.toInt())
            }

            groupLayout.addView(groupTitle)

            val gridLayout = GridLayout(this).apply {
                rowCount = 3
                columnCount = 3
                setPadding(10, 10, 10, 10)
            }

            for ((appName, appIconBitmap, appColor) in apps) {
                val appLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 5, 20, 5)
                }

                val appIconView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(100, 100)
                    setImageBitmap(appIconBitmap)
                }

                val appTextView = TextView(this).apply {
                    text = appName
                    textSize = 14f
                    setPadding(10, 5, 10, 5)
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                appLayout.addView(appIconView)
                appLayout.addView(appTextView)
                gridLayout.addView(appLayout)
            }

            groupLayout.addView(gridLayout)
            folderContainer.addView(groupLayout)
        }
    }
}
