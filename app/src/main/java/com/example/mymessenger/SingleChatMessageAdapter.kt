package com.example.mymessenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessenger.databinding.SingleChatItemBinding
import com.squareup.picasso.Picasso

class SingleChatMessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<SingleChatMessageAdapter.MessageListViewHolder>() {

    private var onMessageLongClickListener: OnMessageLongClickListener? = null

    interface OnMessageLongClickListener {
        fun onMessageLongClick(message: Message, position: Int)
    }

    class MessageListViewHolder(val binding: SingleChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        val binding = SingleChatItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageListViewHolder(binding)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
        val message = messages[position]

        holder.binding.messageChatTV.text = message.text

        if (message.imageUri != null) {
            holder.binding.chatAvatarIV.visibility = View.VISIBLE

            Picasso.get().load(message.imageUri).into(holder.binding.chatAvatarIV)
        } else {
            holder.binding.chatAvatarIV.visibility = View.GONE

            holder.binding.chatAvatarIV.setImageResource(R.drawable.ic_image)
        }
    }

    fun setOnMessageLongClickListener(onMessageLongClickListener: OnMessageLongClickListener) {
        this.onMessageLongClickListener = onMessageLongClickListener
    }

    fun updateMessages(newMessages: MutableList<Message>) {
        messages.clear()
        messages.addAll(newMessages)
    }
}

