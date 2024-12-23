package com.example.mymessenger

import androidx.fragment.app.Fragment

class Tabs(val name: String, val fragment: Fragment) {
    companion object {
        val tabs = listOf(
            Tabs("Чаты", ChatListFragment()),
            Tabs("Пользователи", UserListFragment())
        )
    }
}