package com.example.mymessenger

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymessenger.databinding.FragmentUserListBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private val users: MutableList<User> = mutableListOf()
    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userRV.layoutManager = LinearLayoutManager(requireContext())
        val database = Firebase.database
        val reference = database.getReference("users")
        onChangeListener(reference)
        adapter = UserListAdapter(users)
        binding.userRV.adapter = adapter
        binding.userRV.setHasFixedSize(true)

        adapter.setOnUserClickListener (object :
            UserListAdapter.OnUserClickListener {
            override fun onUserClick(users: User, position: Int) {
                val singleChatFragment = SingleChatFragment()
                findNavController().navigate(R.id.action_mainFragment_to_singleChatFragment)
            }
        })
    }

    private fun onChangeListener(reference: DatabaseReference) {
        reference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val user = i.getValue(User::class.java)
                    if (user != null && !users.contains(user)) {
                        users.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}