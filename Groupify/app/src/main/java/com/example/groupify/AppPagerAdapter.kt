package com.example.groupify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.groupify.models.AppData

class AppPagerAdapter(private val paginatedAppList: List<List<AppData>>) : RecyclerView.Adapter<AppPagerAdapter.AppPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_app_grid, parent, false)
        return AppPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppPageViewHolder, position: Int) {
        val appList = paginatedAppList[position]
        holder.bind(appList)
    }

    override fun getItemCount(): Int = paginatedAppList.size

    class AppPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gridLayout: GridLayout = itemView.findViewById(R.id.gridLayout)

        fun bind(appList: List<AppData>) {
            gridLayout.removeAllViews()

            // 9개 아이콘 표시
            appList.forEach { app ->
                val appLayout = LayoutInflater.from(itemView.context).inflate(R.layout.item_app, null) as LinearLayout
                val appIconView: ImageView = appLayout.findViewById(R.id.appIcon)
                appIconView.setImageDrawable(app.appIcon)
                val appLabel: TextView = appLayout.findViewById(R.id.appLabel)
                appLabel.text = app.appName

                // 앱 클릭 리스너 추가
                appLayout.setOnClickListener {
                    // 앱을 실행할 수 있는 Intent를 가져오기
                    val launchIntent = itemView.context.packageManager.getLaunchIntentForPackage(app.packageName)

                    // 앱이 존재하면 실행
                    launchIntent?.let {
                        itemView.context.startActivity(it)
                    } ?: run {
                        // 앱이 설치되어 있지 않으면 Toast 메시지 표시
                        Toast.makeText(itemView.context, "앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                gridLayout.addView(appLayout)
            }
        }
    }
}
