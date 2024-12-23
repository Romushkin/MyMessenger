package com.example.mymessenger

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsAdapter(fragment: MainFragment, private val tabList: List<Tabs>): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return tabList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = tabList[position].fragment
        return fragment
    }
}