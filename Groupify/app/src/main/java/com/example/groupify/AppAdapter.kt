package com.example.groupify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData
import com.example.groupify.models.FolderData

class AppAdapter(
    private val itemList: List<Any>,
    private val onAppClick: (AppData) -> Unit,
    private val onFolderClick: (FolderData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 두 가지 유형의 뷰홀더 생성: 앱과 폴더
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_APP) {
            val view = inflater.inflate(R.layout.item_app, parent, false)
            AppViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_folder, parent, false)
            FolderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (holder is AppViewHolder && item is AppData) {
            holder.bind(item, onAppClick)
        } else if (holder is FolderViewHolder && item is FolderData) {
            holder.bind(item, onFolderClick)
        }
    }

    override fun getItemCount() = itemList.size

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position] is AppData) VIEW_TYPE_APP else VIEW_TYPE_FOLDER
    }

    companion object {
        const val VIEW_TYPE_APP = 0
        const val VIEW_TYPE_FOLDER = 1
    }

    // AppViewHolder 정의
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appLabel: TextView = itemView.findViewById(R.id.appLabel)

        fun bind(appData: AppData, onClick: (AppData) -> Unit) {
            appIcon.setImageDrawable(appData.appIcon)
            appLabel.text = appData.appName
            itemView.setOnClickListener { onClick(appData) }
        }
    }

    // FolderViewHolder 정의
    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderLabel: TextView = itemView.findViewById(R.id.folderLabel)

        fun bind(folderData: FolderData, onClick: (FolderData) -> Unit) {
            folderLabel.text = folderData.folderName
            itemView.setOnClickListener { onClick(folderData) }
        }
    }
}
