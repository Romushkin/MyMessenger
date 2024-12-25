package com.example.mymessenger.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.R
import com.example.mymessenger.adapters.UserListAdapter
import com.example.mymessenger.models.User
import com.google.firebase.auth.auth

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private val users: MutableList<User> = mutableListOf()
    private lateinit var adapter: UserListAdapter
    private val allUsers: MutableList<User> = mutableListOf()

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
        val auth = Firebase.auth
        val reference = database.getReference("users")
        onChangeListener(reference)
        adapter = UserListAdapter(users)
        binding.userRV.adapter = adapter
        binding.userRV.setHasFixedSize(true)

        adapter.setOnUserClickListener(object :
            UserListAdapter.OnUserClickListener {
            override fun onUserClick(user: User, position: Int) {
                val currentId = auth.currentUser?.uid
                val chatID = getChatID(currentId.toString(), user.id)
                if (currentId != null) {
                    val bundle = Bundle()
                    if (user.name.isEmpty()) {
                        bundle.putString("name", user.email)
                    } else {
                        bundle.putString("name", user.name)
                    }
                    bundle.putString("chatID", chatID)
                    bundle.putString("profileImageUri", user.profileImageUri)
                    findNavController().navigate(
                        R.id.action_mainFragment_to_singleChatFragment,
                        bundle
                    )
                }

            }
        })

        binding.searchET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filteredUsers = allUsers.filter { user ->
                    user.name.lowercase().contains(query) || user.email.lowercase().contains(query)
                }
                updateUserList(filteredUsers)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    private fun onChangeListener(reference: DatabaseReference) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allUsers.clear()
                for (i in snapshot.children) {
                    val user = i.getValue(User::class.java)
                    if (user != null) {
                        allUsers.add(user)
                    }
                    updateUserList(allUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }

        })
    }

    private fun updateUserList(filteredUsers: List<User>) {
        users.clear()
        users.addAll(filteredUsers)
        adapter.notifyDataSetChanged()
    }

    private fun getChatID(currentUserID: String, selectedUserID: String): String {
        val users = listOf(currentUserID, selectedUserID)
        return users.sorted().joinToString("-")
    }
}