package com.example.groupify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        private val folderPreview1: ImageView = itemView.findViewById(R.id.folderPreview1)
        private val folderPreview2: ImageView = itemView.findViewById(R.id.folderPreview2)
        private val folderLabel: TextView = itemView.findViewById(R.id.folderLabel)

        fun bind(clusterNumber: Int, appList: List<AppData>) {
            // 폴더 이름 설정
            folderLabel.text = "Folder $clusterNumber"

            // 불투명한 정사각형 배경 설정 (80% 투명도)
            itemView.setBackgroundResource(R.drawable.ic_folder_background)

            // 앱 아이콘 미리보기 설정 (최대 2개)
            if (appList.isNotEmpty()) {
                folderPreview1.setImageDrawable(appList[0].appIcon)
                folderPreview1.visibility = View.VISIBLE
            } else {
                folderPreview1.visibility = View.GONE
            }

            if (appList.size > 1) {
                folderPreview2.setImageDrawable(appList[1].appIcon)
                folderPreview2.visibility = View.VISIBLE
            } else {
                folderPreview2.visibility = View.GONE
            }

            // 폴더 클릭 시 앱 목록을 보여줌
            itemView.setOnClickListener {
                onFolderClick(clusterNumber, appList)
            }
        }
    }
}
