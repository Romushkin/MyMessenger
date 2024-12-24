package com.example.mymessenger

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymessenger.databinding.FragmentChatListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private var users: MutableList<User> = mutableListOf()
    private lateinit var adapter: ChatListAdapter
    val database = Firebase.database
    val auth = Firebase.auth
    val reference = database.getReference("chats")

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
        onChangeListener(reference)
        adapter = ChatListAdapter(users)
        binding.chatRV.adapter = adapter
        binding.chatRV.setHasFixedSize(true)

        adapter.setOnChatClickListener(object :
            ChatListAdapter.OnChatClickListener {
            override fun onChatClick(user: User, position: Int) {
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
                    findNavController().navigate(
                        R.id.action_mainFragment_to_singleChatFragment,
                        bundle
                    )
                }
            }
        })
    }

    private fun onChangeListener(reference: DatabaseReference) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentId = auth.currentUser?.uid
                for (chatSnapshot in snapshot.children) {
                    if (currentId != null) {
                        if (chatSnapshot.toString().contains(currentId)) {
                            val chatmateId = getChatmateId(currentId, chatSnapshot.key.toString())
                            val lastMessage =
                                chatSnapshot.child("messages").children.last().children.last().value
                            val userRef = database.getReference("users").child(chatmateId)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    if (user != null) {
                                        val existingUserIndex =
                                            users.indexOfFirst { it.id == user.id }
                                        if (existingUserIndex != -1) {
                                            users[existingUserIndex].lastMessage =
                                                lastMessage.toString()
                                            adapter.notifyItemChanged(existingUserIndex)
                                        } else {
                                            user.lastMessage = lastMessage.toString()
                                            users.add(user)
                                            adapter.notifyItemInserted(users.size - 1)
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(
                                        "ChatsFragment",
                                        "Failed to retrieve user data: $databaseError"
                                    )
                                }
                            })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }

        })
    }

    private fun getChatID(currentUserID: String, selectedUserID: String): String {
        val users = listOf(currentUserID, selectedUserID)
        return users.sorted().joinToString("-")
    }

    private fun getChatmateId(currentUserId: String, chatId: String): String {
        val userIds = chatId.split("-")
        return if (userIds[0] == currentUserId) {
            userIds[1]
        } else {
            userIds[0]
        }
    }
}