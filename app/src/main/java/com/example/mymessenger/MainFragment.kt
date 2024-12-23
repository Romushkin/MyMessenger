package com.example.mymessenger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymessenger.Tabs.Companion.tabs
import com.example.mymessenger.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TabsAdapter(this, tabs)
        binding.mainViewPagerVP.adapter = adapter
        TabLayoutMediator(binding.mainTabLayoutTL, binding.mainViewPagerVP) { tab, position ->
            tab.text = tabs[position].name
        }.attach()
    }
}