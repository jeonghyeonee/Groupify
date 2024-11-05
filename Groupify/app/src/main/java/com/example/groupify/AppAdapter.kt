package com.example.groupify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AppAdapter(
    private val apps: List<AppData> // ClusterData 대신 List<AppData>로 변경
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appIconImageView: ImageView = view.findViewById(R.id.appIconImageView)
        private val appNameTextView: TextView = view.findViewById(R.id.appNameTextView)

        fun bind(appData: AppData) {
            appNameTextView.text = appData.name
            Glide.with(itemView.context).load(appData.iconUrl).into(appIconImageView)
        }
    }
}
