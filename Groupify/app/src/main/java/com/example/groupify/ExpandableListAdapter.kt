package com.example.groupify

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class ExpandableListAdapter(
    private val context: Context,
    private val groupList: List<String>,
    private val itemList: Map<String, List<Pair<String, String>>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = groupList.size

    override fun getChildrenCount(groupPosition: Int): Int = itemList[groupList[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groupList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        itemList[groupList[groupPosition]]?.get(childPosition) ?: Pair("", "")

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupName = getGroup(groupPosition) as String
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = groupName
        return view
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_item, null)
        val textView = view.findViewById<TextView>(R.id.itemTextView)
        val imageView = view.findViewById<ImageView>(R.id.itemImageView)

        val (appName, color) = getChild(groupPosition, childPosition) as Pair<String, String>

        textView.text = appName
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        textView.setBackgroundColor(Color.parseColor(color))

        try {
            val packageManager: PackageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            var appIconFound = false

            for (appInfo in installedApps) {
                val installedAppName = appInfo.loadLabel(packageManager).toString()
                if (installedAppName.equals(appName, ignoreCase = true)) {
                    imageView.setImageDrawable(appInfo.loadIcon(packageManager))
                    appIconFound = true
                    break
                }
            }

            if (!appIconFound) {
                // 아이콘을 찾지 못한 경우 기본 아이콘 사용
                imageView.setImageResource(R.drawable.default_icon)
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.default_icon) // 기본 아이콘 설정
        }

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
