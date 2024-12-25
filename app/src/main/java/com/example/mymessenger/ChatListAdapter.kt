package com.example.mymessenger

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessenger.databinding.ChatListItemBinding
import com.squareup.picasso.Picasso

class ChatListAdapter(private val users: MutableList<User>) :
    RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private var onChatClickListener: OnChatClickListener? = null

    interface OnChatClickListener {
        fun onChatClick(user: User, position: Int)
    }

    class ChatListViewHolder(val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding = ChatListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val user = users[position]
        if (user.name.isEmpty()) {
            holder.binding.chatUserNameTV.text = user.email
        } else
            holder.binding.chatUserNameTV.text = user.name
        holder.binding.lastMessageTV.text = user.lastMessage
        if (user.profileImageUri.isNotEmpty()) {
            Picasso.get().load(user.profileImageUri).placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person).into(holder.binding.avatarIV)
        } else {
            holder.binding.avatarIV.setImageResource(R.drawable.ic_person)
        }

        if (user.isOnline) {
            holder.binding.statusIV.visibility = View.VISIBLE
        } else {
            holder.binding.statusIV.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            if (onChatClickListener != null) {
                onChatClickListener!!.onChatClick(user, position)
            }
        }
    }

    fun setOnChatClickListener(onChatClickListener: OnChatClickListener) {
        this.onChatClickListener = onChatClickListener
    }
}


