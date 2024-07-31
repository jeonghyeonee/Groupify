package com.example.groupify

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FolderViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_view)

        val colorGroups = intent.getSerializableExtra("colorGroups") as HashMap<Int, ArrayList<AppData>>
        val folderContainer: GridLayout = findViewById(R.id.folderContainer)

        for ((groupId, apps) in colorGroups) {
            val groupLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 10, 10, 10)
                setBackgroundColor(Color.LTGRAY) // 폴더 배경을 회색으로 설정
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }

            val groupTitle = TextView(this).apply {
                text = "Group ${groupId + 1}"
                textSize = 18f
                setPadding(10, 10, 10, 10)
                setBackgroundColor(Color.LTGRAY) // 폴더 배경을 회색으로 설정
                setTextColor(Color.BLACK) // 폴더 이름을 검정색으로 설정
            }

            val gridLayout = GridLayout(this).apply {
                rowCount = 3
                columnCount = 3
                setPadding(10, 10, 10, 10)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            apps.take(9).forEach { app ->
                val appLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(5, 5, 5, 5)
                }

                val appIconView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(100, 100)
                    val iconFile = File(getExternalFilesDir(null), "AppIcons/${app.iconFileName}")
                    if (iconFile.exists()) {
                        setImageBitmap(BitmapFactory.decodeFile(iconFile.absolutePath))
                    }
                }

                val appTextView = TextView(this).apply {
                    text = app.name
                    textSize = 12f
                    setPadding(5, 5, 5, 5)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }

                appLayout.addView(appIconView)
                appLayout.addView(appTextView)
                gridLayout.addView(appLayout)
            }

            groupLayout.addView(groupTitle)
            groupLayout.addView(gridLayout)
            folderContainer.addView(groupLayout)
        }
    }
}
