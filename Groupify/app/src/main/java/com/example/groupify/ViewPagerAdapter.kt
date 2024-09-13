package com.example.groupify

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, private val clusterMap: Map<String, List<Pair<String, String>>>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return clusterMap.size // 클러스터 수만큼 페이지 생성
    }

    override fun getItem(position: Int): Fragment {
        val cluster = clusterMap.keys.elementAt(position)
        Log.d("ViewPagerAdapter", "Creating fragment for cluster: $cluster")
        return AppPageFragment.newInstance(clusterMap[cluster] ?: emptyList())
    }

    override fun getPageTitle(position: Int): CharSequence {
        val cluster = clusterMap.keys.elementAt(position)
        return "Cluster $cluster"
    }
}


