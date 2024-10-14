package com.example.groupify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment

class HomeScreenFragment : Fragment() {

    companion object {
        fun newInstance(page: Int): HomeScreenFragment {
            val fragment = HomeScreenFragment()
            val args = Bundle()
            args.putInt("page", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)
        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        loadAppsForPage(gridLayout)
        return view
    }

    private fun loadAppsForPage(gridLayout: GridLayout) {
        // 앱을 페이지별로 로드 (이전 `createAppLayout` 메서드 사용)
    }
}
