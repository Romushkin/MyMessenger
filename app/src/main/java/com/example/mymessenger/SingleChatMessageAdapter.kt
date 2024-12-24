package com.example.mymessenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class SingleChatMessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<SingleChatMessageAdapter.MessageListViewHolder>() {

    private var onMessageLongClickListener: OnMessageLongClickListener? = null

    interface OnMessageLongClickListener {
        fun onMessageLongClick(message: Message, position: Int)
    }

    class MessageListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val messageChatTV: TextView = itemView.findViewById(R.id.messageChatTV)
        val chatAvatarIV: ImageView = itemView.findViewById(R.id.chatAvatarIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return MessageListViewHolder(view)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
        val message = messages[position]

        holder.messageChatTV.text = message.text

        if (message.imageUri != null) {
            holder.chatAvatarIV.visibility = View.VISIBLE

            Picasso.get().load(message.imageUri).into(holder.chatAvatarIV)
        } else {
            holder.chatAvatarIV.visibility = View.GONE

            holder.chatAvatarIV.setImageResource(R.drawable.ic_image)
        }

        holder.itemView.setOnLongClickListener {
            onMessageLongClickListener?.onMessageLongClick(message, position)
            true
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].currentUserID == FirebaseAuth.getInstance().currentUser?.uid)
            R.layout.single_chat_currentuser_item
        else
            R.layout.single_chat_chatmate_item
    }

    fun setOnMessageLongClickListener(onMessageLongClickListener: OnMessageLongClickListener) {
        this.onMessageLongClickListener = onMessageLongClickListener
    }

    fun updateMessages(newMessages: MutableList<Message>) {
        messages.clear()
        messages.addAll(newMessages)
    }
}

