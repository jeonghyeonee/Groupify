package com.example.groupify

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
class FolderAdapter(
    private val folderMap: MutableMap<String, MutableList<AppData>>, // 'predicted_color'를 키로 사용
    private var isNameMode: Boolean, // 이름 모드
    private var isFruitMode: Boolean, // 과일 모드 추가
    private val onFolderClick: (String, List<AppData>) -> Unit // 폴더 클릭 시 호출될 콜백
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    fun updateFolderNames(isNameMode: Boolean, isFruitMode: Boolean) {
        this.isNameMode = isNameMode
        this.isFruitMode = isFruitMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val predictedColor = folderMap.keys.elementAt(position)  // predicted_color 사용
        val appList = folderMap[predictedColor] ?: emptyList()

        holder.bind(predictedColor, appList) // 폴더 이름에 predictedColor 사용
    }

    override fun getItemCount(): Int = folderMap.size

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderPreviewGrid: GridLayout = itemView.findViewById(R.id.folderPreviewGrid)
        private val folderLabel: TextView = itemView.findViewById(R.id.folderLabel)

        fun bind(predictedColor: String, appList: List<AppData>) {
            // 폴더 이름을 predicted_color로 설정
            folderLabel.text = when {
                isNameMode -> predictedColor // 이름 모드에서는 predicted_color 표시
                isFruitMode -> getFruitEmojiForColor(predictedColor) // 과일 모드에서는 과일 이모티콘 표시
                else -> getEmojiForColor(predictedColor) // 이모티콘 모드에서는 이모티콘으로 표시
            }

            // 폴더의 앱 아이콘을 최대 9개까지 그리드로 설정
            folderPreviewGrid.removeAllViews()

            val maxIcons = 9 // 최대 9개 아이콘 표시
            val iconCount = minOf(appList.size, maxIcons)

            try {
                for (i in 0 until iconCount) {
                    val appIconView = ImageView(itemView.context).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 50 // 원하는 크기
                            height = 50
                        }
                        setImageDrawable(appList[i].appIcon)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    folderPreviewGrid.addView(appIconView) // 그리드에 아이콘 추가
                }
            } catch (e: Exception) {
                Log.e("FolderAdapter", "아이콘 추가 중 오류 발생", e)
                Toast.makeText(itemView.context, "아이콘 추가 중 오류 발생", Toast.LENGTH_SHORT).show()
            }

            // 폴더 클릭 시 앱 목록을 보여줌
            itemView.setOnClickListener {
                onFolderClick(predictedColor, appList) // predictedColor를 전달
            }
        }

        // 색상 이름을 이모티콘으로 변환하는 함수
        private fun getEmojiForColor(color: String): String {
            return when (color) {
                "red" -> "❤️"
                "yellow" -> "💛"
                "green" -> "💚"
                "blue" -> "💙"
                "white" -> "🤍"
                "purple" -> "💜"
                "pink" -> "🩷"
                "orange" -> "🧡"
                "black" -> "🖤"
                "brown" -> "🤎"
                else -> color
            }
        }

        // 과일 이모티콘 반환 함수
        private fun getFruitEmojiForColor(color: String): String {
            return when (color) {
                "red" -> "🍎"
                "yellow" -> "🍌"
                "green" -> "🥑"
                "blue" -> "🫐"
                "white" -> "🥛"
                "purple" -> "🍇"
                "pink" -> "🍑"
                "orange" -> "🍊"
                "black" -> "🍙"
                "brown" -> "🍫"
                else -> color
            }
        }
    }
}
