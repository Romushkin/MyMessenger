package com.example.mymessenger.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mymessenger.fragments.MainFragment
import com.example.mymessenger.models.Tabs

class TabsAdapter(fragment: MainFragment, private val tabList: List<Tabs>): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return tabList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = tabList[position].fragment
        return fragment
    }
}