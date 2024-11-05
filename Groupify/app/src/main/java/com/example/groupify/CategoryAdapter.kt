package com.example.groupify

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.AppData
import com.example.groupify.R

class CategoryAdapter(
    private val appList: List<AppData>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<CategoryAdapter.AppViewHolder>() {

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIconImageView)
        val appName: TextView = itemView.findViewById(R.id.appNameTextView)

        fun bind(app: AppData) {
            appName.text = app.name
            // 앱 아이콘 로드 (Glide 사용 가능)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(appList[position])
    }

    override fun getItemCount(): Int = appList.size
}
