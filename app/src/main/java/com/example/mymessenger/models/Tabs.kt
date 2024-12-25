package com.example.mymessenger.models

import androidx.fragment.app.Fragment
import com.example.mymessenger.fragments.ChatListFragment
import com.example.mymessenger.fragments.UserListFragment

class Tabs(val name: String, val fragment: Fragment) {
    companion object {
        val tabs = listOf(
            Tabs("Чаты", ChatListFragment()),
            Tabs("Пользователи", UserListFragment())
        )
    }
}