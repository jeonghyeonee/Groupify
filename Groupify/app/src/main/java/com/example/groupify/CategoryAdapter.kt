package com.example.groupify

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categorizedApps: Map<String, List<ApplicationInfo>>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: TextView = view.findViewById(R.id.categoryName)
        val appContainer: ViewGroup = view.findViewById(R.id.appContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categorizedApps.keys.toList()[position]
        val apps = categorizedApps[category] ?: emptyList()

        holder.categoryTextView.text = category

        // 앱 아이콘과 이름을 동적으로 추가
        holder.appContainer.removeAllViews()  // 이전 항목이 있다면 제거
        for (appInfo in apps) {
            val appView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_app, holder.appContainer, false)

            val appNameView = appView.findViewById<TextView>(R.id.appName)
            val appIconView = appView.findViewById<ImageView>(R.id.appIcon)

            appNameView.text = packageManager.getApplicationLabel(appInfo).toString()
            appIconView.setImageDrawable(packageManager.getApplicationIcon(appInfo))

            // 앱 클릭 이벤트 처리 (앱 실행)
            appView.setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    holder.itemView.context.startActivity(launchIntent)
                }
            }

            holder.appContainer.addView(appView)
        }
    }

    override fun getItemCount(): Int = categorizedApps.size
}
