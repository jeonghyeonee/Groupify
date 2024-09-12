package com.example.groupify

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class AppPageFragment : Fragment() {

    private lateinit var apps: List<Pair<String, String>>

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_page, container, false)
        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)

        // 각 앱을 GridLayout에 추가
        for ((appName, packageName) in apps) {
            val appLayout = createAppLayout(appName, packageName)
            gridLayout.addView(appLayout)
        }

        return view
    }

    // 앱 아이콘과 이름을 표시하는 레이아웃 생성
    private fun createAppLayout(appName: String, packageName: String): LinearLayout {
        val appLayout = LinearLayout(context)
        appLayout.orientation = LinearLayout.VERTICAL
        appLayout.setPadding(8, 8, 8, 8)

        val imageView = ImageView(context)
        try {
            val appIcon = requireContext().packageManager.getApplicationIcon(packageName)
            imageView.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            imageView.setImageResource(R.drawable.default_icon)
        }

        imageView.layoutParams = LinearLayout.LayoutParams(150, 150)

        val textView = TextView(context)
        textView.text = appName
        textView.setTextColor(Color.BLACK)

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱 아이콘 클릭 시 앱 실행
        imageView.setOnClickListener {
            val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        return appLayout
    }
}
