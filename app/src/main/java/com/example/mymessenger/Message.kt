package com.example.mymessenger

data class Message(
    var id: String,
    var currentUserID: String,
    var text: String,
    var imageUri: String? = null,
    val readMessage: Boolean = false
)