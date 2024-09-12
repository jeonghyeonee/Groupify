package com.example.groupify

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 3 // 총 3개의 페이지를 설정
    }

    override fun getItem(position: Int): Fragment {
        return HomeScreenFragment.newInstance(position)
    }
}
