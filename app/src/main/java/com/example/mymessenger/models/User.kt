package com.example.mymessenger.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val role: String = "",
    val address: String = "",
    val age: String = "",
    val phone: String = "",
    var lastMessage: String = "",
    var isOnline: Boolean = false,
    val profileImageUri: String = ""
)

