package com.example.groupify

import android.content.ClipData
import android.content.ClipDescription
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import android.app.AlertDialog
import android.util.Log
import android.widget.FrameLayout

class AppPageFragment : Fragment() {

    private lateinit var apps: List<Pair<String, String>>  // Pair(packageName, cluster)

    companion object {
        fun newInstance(apps: List<Pair<String, String>>): AppPageFragment {
            val fragment = AppPageFragment()
            val args = Bundle()
            args.putSerializable("apps", ArrayList(apps))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apps = arguments?.getSerializable("apps") as List<Pair<String, String>>
        Log.d("AppPageFragment", "Received ${apps.size} apps in this fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_page, container, false)
        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)

        // 클러스터별로 폴더 생성
        val groupedApps = apps.groupBy { it.second } // 클러스터를 기준으로 앱 그룹화
        for ((cluster, appList) in groupedApps) {
            val folderLayout = createFolderLayout(cluster, appList)
            gridLayout.addView(folderLayout)
        }

        return view
    }

    // 클러스터별 폴더 생성
    private fun createFolderLayout(cluster: String, appList: List<Pair<String, String>>): LinearLayout {
        val folderLayout = LinearLayout(requireContext())  // requireContext() 사용
        folderLayout.orientation = LinearLayout.VERTICAL
        folderLayout.setPadding(8, 8, 8, 8)

        // 폴더 아이콘 생성
        val folderIcon = ImageView(requireContext())  // requireContext() 사용
        folderIcon.setImageResource(R.drawable.baseline_folder_open_24) // 폴더 배경 이미지
        folderIcon.layoutParams = LinearLayout.LayoutParams(150, 150)

        // 미리보기로 보여줄 앱 아이콘 2~3개 겹치기
        val previewContainer = FrameLayout(requireContext())  // requireContext() 사용
        previewContainer.layoutParams = FrameLayout.LayoutParams(150, 150)

        val maxPreviewApps = 3
        for (i in 0 until minOf(maxPreviewApps, appList.size)) {
            val appIcon = ImageView(requireContext())  // requireContext() 사용
            val packageName = appList[i].first
            try {
                val icon = requireContext().packageManager.getApplicationIcon(packageName)
                appIcon.setImageDrawable(icon)
                val size = 60 - (i * 10) // 겹치는 앱 아이콘 크기 줄이기
                val params = FrameLayout.LayoutParams(size, size)
                params.setMargins(i * 20, i * 20, 0, 0) // 각 아이콘 위치 겹치기
                appIcon.layoutParams = params
                previewContainer.addView(appIcon)
            } catch (e: PackageManager.NameNotFoundException) {
                appIcon.setImageResource(R.drawable.default_icon)
            }
        }

        folderLayout.addView(folderIcon)
        folderLayout.addView(previewContainer)

        // 폴더 클릭 시 앱 목록 표시
        folderLayout.setOnClickListener {
            showFolderDialog(appList)
        }

        return folderLayout
    }



    // 폴더 클릭 시 앱 목록을 보여주는 다이얼로그
    private fun showFolderDialog(appList: List<Pair<String, String>>) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Apps in this Folder")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        // 앱 목록을 동적으로 추가
        for ((packageName, _) in appList) {
            val appLayout = createAppLayout(packageName)
            layout.addView(appLayout)
        }

        dialog.setView(layout)
        dialog.setNegativeButton("Close", null)
        dialog.show()
    }

    // 앱 아이콘과 이름을 표시하는 레이아웃 생성
    private fun createAppLayout(packageName: String): LinearLayout {
        val appLayout = LinearLayout(context)
        appLayout.orientation = LinearLayout.HORIZONTAL
        appLayout.setPadding(8, 8, 8, 8)

        val imageView = ImageView(context)
        try {
            val appIcon = requireContext().packageManager.getApplicationIcon(packageName)
            imageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            imageView.setImageResource(R.drawable.default_icon)
        }

        imageView.layoutParams = LinearLayout.LayoutParams(100, 100)

        val textView = TextView(context)
        try {
            val appName = requireContext().packageManager.getApplicationLabel(requireContext().packageManager.getApplicationInfo(packageName, 0))
            textView.text = appName
        } catch (e: PackageManager.NameNotFoundException) {
            textView.text = packageName
        }
        textView.setPadding(16, 0, 0, 0)

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱 클릭 시 실행
        appLayout.setOnClickListener {
            val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        return appLayout
    }
}
