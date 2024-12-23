package com.example.mymessenger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessenger.databinding.UserListItemBinding

class UserListAdapter(private val users: MutableList<User>) :
    RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    private var onUserClickListener: OnUserClickListener? = null

    interface OnUserClickListener {
        fun onUserClick(users: User, position: Int)
    }

    class UserListViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = UserListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return UserListViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val user = users[position]
        if (user.name.isEmpty()) {
            holder.binding.userNameTV.text = user.email
        } else
            holder.binding.userNameTV.text = user.name

        holder.itemView.setOnClickListener {
            if (onUserClickListener != null) {
                onUserClickListener!!.onUserClick(user, position)
            }
        }
    }

    fun setOnUserClickListener(onUserClickListener: OnUserClickListener) {
        this.onUserClickListener = onUserClickListener
    }
}