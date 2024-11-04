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
    private val onFolderClick: (Int, List<AppData>) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var folderNames: List<String> = emptyList()

    fun updateFolderNames(namesList: List<String>) {
        folderNames = namesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val clusterNumber = folderMap.keys.elementAt(position)
        val appList = folderMap[clusterNumber] ?: emptyList()
        val folderName = folderNames.getOrNull(position) ?: "Folder $clusterNumber"
        holder.bind(clusterNumber, appList, folderName)
    }

    override fun getItemCount(): Int = folderMap.size

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderPreview1: ImageView = itemView.findViewById(R.id.folderPreview1)
        private val folderPreview2: ImageView = itemView.findViewById(R.id.folderPreview2)
        private val folderLabel: TextView = itemView.findViewById(R.id.folderLabel)

        fun bind(clusterNumber: Int, appList: List<AppData>, folderName: String) {
            folderLabel.text = folderName
            itemView.setBackgroundResource(R.drawable.ic_folder_background)

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

            itemView.setOnClickListener {
                onFolderClick(clusterNumber, appList)
            }
        }
    }
}
