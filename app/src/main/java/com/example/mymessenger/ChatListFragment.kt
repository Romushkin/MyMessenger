package com.example.mymessenger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymessenger.databinding.FragmentChatListBinding

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private var chats: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatRV.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ChatListAdapter(chats)
        binding.chatRV.adapter = adapter
        binding.chatRV.setHasFixedSize(true)
        adapter.setOnChatClickListener(object :
            ChatListAdapter.OnChatClickListener {
            override fun onChatClick(users: User, position: Int) {

            }
        })
    }

}