package com.example.mymessenger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessenger.databinding.ChatListItemBinding

class ChatListAdapter(private val chats: MutableList<User>) :
    RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private var onChatClickListener: OnChatClickListener? = null

    interface OnChatClickListener {
        fun onChatClick(users: User, position: Int)
    }

    class ChatListViewHolder(val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding = ChatListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chat = chats[position]
        if (chat.name.isEmpty()) {
            holder.binding.chatUserNameTV.text = chat.email
        } else
            holder.binding.chatUserNameTV.text = chat.name
        holder.binding.lastMessageTV.text = chat.lastMessage
        holder.itemView.setOnClickListener {
            if (onChatClickListener != null) {
                onChatClickListener!!.onChatClick(chat, position)
            }
        }
    }

    fun setOnChatClickListener(onChatClickListener: OnChatClickListener) {
        this.onChatClickListener = onChatClickListener
    }
}


