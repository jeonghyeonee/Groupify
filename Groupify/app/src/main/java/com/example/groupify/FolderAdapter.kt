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
    private val folderMap: Map<Int, List<AppData>>,
    private val onFolderClick: (Int, List<AppData>) -> Unit // 폴더 클릭 시 호출될 콜백
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val clusterNumber = folderMap.keys.elementAt(position)
        val appList = folderMap[clusterNumber] ?: emptyList()

        holder.bind(clusterNumber, appList)
    }

    override fun getItemCount(): Int = folderMap.size

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderPreviewGrid: GridLayout = itemView.findViewById(R.id.folderPreviewGrid)
        private val folderLabel: TextView = itemView.findViewById(R.id.folderLabel)

        fun bind(clusterNumber: Int, appList: List<AppData>) {
            // 폴더 이름 설정
            folderLabel.text = "Folder $clusterNumber"

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
                onFolderClick(clusterNumber, appList)
            }
        }
    }
}
