package com.example.groupify

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, private val pages: List<List<Pair<String, String>>>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return pages.size // 페이지 수 반환
    }

    override fun getItem(position: Int): Fragment {
        // 각 페이지에 해당하는 Fragment를 반환
        return AppPageFragment.newInstance(pages[position])
    }
}
