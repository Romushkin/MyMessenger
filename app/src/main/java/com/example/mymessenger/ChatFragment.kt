package com.example.mymessenger

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessenger.databinding.FragmentChatBinding
import com.example.mymessenger.databinding.FragmentFirstBinding
import com.example.mymessenger.databinding.FragmentForgotPasswordBinding
import com.example.mymessenger.databinding.FragmentUserBinding
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}