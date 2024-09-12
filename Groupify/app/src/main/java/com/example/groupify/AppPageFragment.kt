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

class AppPageFragment : Fragment() {

    private lateinit var apps: List<Pair<String, String>>
    private var draggedApp: Pair<String, String>? = null

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

        // GridLayout에 드래그 리스너 추가 (폴더 생성을 위해)
        gridLayout.setOnDragListener(folderCreationListener)

        return view
    }

    // 앱 아이콘과 이름을 표시하는 레이아웃 생성 및 드래그 가능하게 설정
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

        // 패키지 이름을 태그로 설정
        appLayout.tag = packageName

        appLayout.addView(imageView)
        appLayout.addView(textView)

        // 앱을 드래그할 수 있도록 설정
        appLayout.setOnLongClickListener {
            draggedApp = Pair(appName, packageName) // 드래그된 앱 저장
            val clipText = "$appName:$packageName"
            val item = ClipData.Item(clipText)

            val dragData = ClipData(
                clipText,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )

            val shadow = View.DragShadowBuilder(it)
            it.startDragAndDrop(dragData, shadow, it, 0)

            true
        }

        // 폴더로 묶기 위한 드롭 리스너 추가
        appLayout.setOnDragListener(appDragListener)

        return appLayout
    }

    // 앱을 드래그하여 폴더로 묶을 수 있게 하는 드롭 리스너
    private val appDragListener = View.OnDragListener { view, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.setBackgroundColor(Color.LTGRAY) // 드래그 대상 강조
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.setBackgroundColor(Color.TRANSPARENT) // 강조 해제
                true
            }
            DragEvent.ACTION_DROP -> {
                val targetAppLayout = view as LinearLayout
                val targetApp = targetAppLayout.getChildAt(1) as TextView // 타겟 앱 이름 가져오기

                if (draggedApp != null) {
                    // 폴더 생성 및 앱 묶기
                    createFolder(draggedApp!!, targetApp.text.toString(), targetAppLayout)
                    draggedApp = null // 드래그 초기화
                }
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.setBackgroundColor(Color.TRANSPARENT)
                true
            }
            else -> false
        }
    }

    // 폴더 생성 및 앱 묶는 함수
    private fun createFolder(draggedApp: Pair<String, String>, targetAppName: String, targetAppLayout: LinearLayout) {
        val folderLayout = LinearLayout(context)
        folderLayout.orientation = LinearLayout.VERTICAL
        folderLayout.setBackgroundColor(Color.LTGRAY)
        folderLayout.setPadding(8, 8, 8, 8)

        // 폴더 아이콘 및 이름 설정
        val folderName = TextView(context)
        folderName.text = "Folder"
        folderName.setTextColor(Color.BLACK)
        folderLayout.addView(folderName)

        // 폴더 안에 앱들을 담는 리스트
        val folderApps = mutableListOf<Pair<String, String>>()
        folderApps.add(draggedApp) // 드래그된 앱
        val targetPackageName = targetAppLayout.tag as String // 패키지 이름을 태그에서 가져오기
        folderApps.add(Pair(targetAppName, targetPackageName)) // 타겟 앱 추가

        // 폴더 클릭 시 앱들을 보여주는 다이얼로그 띄우기
        folderLayout.setOnClickListener {
            showFolderDialog(folderApps)
        }

        // 기존의 앱 레이아웃 삭제하고 폴더로 대체
        val parentLayout = targetAppLayout.parent as ViewGroup
        parentLayout.removeView(targetAppLayout)
        parentLayout.addView(folderLayout)
    }

    // 폴더 클릭 시 앱 목록을 보여주는 다이얼로그
    private fun showFolderDialog(folderApps: List<Pair<String, String>>) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Folder Apps")

        // 폴더 안에 있는 앱들을 LinearLayout에 동적으로 추가
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        for ((appName, packageName) in folderApps) {
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

            val textView = TextView(context)
            textView.text = appName
            textView.setPadding(8, 0, 0, 0)

            appLayout.addView(imageView)
            appLayout.addView(textView)

            // 앱 아이콘 클릭 시 앱 실행
            appLayout.setOnClickListener {
                val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                }
            }

            layout.addView(appLayout)
        }

        dialog.setView(layout)
        dialog.setNegativeButton("Close", null)
        dialog.show()
    }

    // GridLayout을 대상으로 폴더를 생성하는 드래그 리스너
    private val folderCreationListener = View.OnDragListener { _, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true
            DragEvent.ACTION_DROP -> {
                // 빈 공간에 드롭했을 때 폴더 생성 안 함
                true
            }
            else -> true
        }
    }
}
