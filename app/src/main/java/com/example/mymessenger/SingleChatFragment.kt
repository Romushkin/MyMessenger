package com.example.mymessenger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymessenger.databinding.FragmentSingleChatBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class SingleChatFragment : Fragment() {

    private var _binding: FragmentSingleChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SingleChatMessageAdapter
    private val database = Firebase.database
    private val auth = Firebase.auth
    private lateinit var chatId: String
    private var selectedFileUri: Uri? = null
    private var isFileSelected: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleChatBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messagesRV.layoutManager = LinearLayoutManager(requireContext())
        val userName = arguments?.getString("name").toString()
        binding.chatTitleTV.text = userName
        chatId = arguments?.getString("chatID").toString()

        binding.sendMessageIB.setOnClickListener {
            // Проверяем, был ли выбран файл
            if (isFileSelected) {
                // Если файл выбран, отправляем его как вложение
                selectedFileUri?.let {
                    // Отправляем сообщение с файлом (или изображением)
                    sendMessageWithAttachment(it, binding.editMessageET.text.toString())

                    // После отправки сбрасываем состояния выбора файла
                    binding.attachImageIB.setImageResource(R.drawable.ic_image) // Заглушка или иконка вложения
                    selectedFileUri = null // Очищаем URI
                    isFileSelected = false // Убираем флаг выбора файла
                    binding.editMessageET.text.clear() // Очищаем поле ввода сообщения
                }
            } else {
                // Если файл не выбран, отправляем только текстовое сообщение
                val messageText = binding.editMessageET.text.toString()
                if (messageText.isNotEmpty()) {
                    // Отправляем текстовое сообщение
                    sendMessage(messageText)

                    // Очищаем поле ввода после отправки
                    binding.editMessageET.text.clear()
                } else {
                    // Если поле ввода пустое, показываем сообщение
                    Toast.makeText(
                        requireContext(),
                        "Сообщение не может быть пустым",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        adapter = SingleChatMessageAdapter(mutableListOf())
        binding.messagesRV.adapter = adapter
        binding.messagesRV.setHasFixedSize(true)

        adapter.setOnMessageLongClickListener(object :
            SingleChatMessageAdapter.OnMessageLongClickListener {
            override fun onMessageLongClick(message: Message, position: Int) {
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("Что вы хотите выполнить?")
                if (message.imageUri != null) {
                    builder.setPositiveButton("Посмотреть полное изображение") { dialog, _ ->
                        message.imageUri?.let { showImageDialog(it) }
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("Удалить") { dialog, _ ->
                        deleteMessage(message)
                        dialog.dismiss()
                    }
                } else {
                    builder.setPositiveButton("Удалить сообщение") { dialog, _ ->
                        deleteMessage(message)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                }
                builder.show()
            }
        })

        binding.messagesRV.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.messagesRV.postDelayed({
                    binding.messagesRV.scrollToPosition(adapter.itemCount - 1)
                }, 100)
            }
        }
        chatWithUser(
            database.reference.child("chats")
                .child(chatId).child("messages")
        )

    }

    private fun sendMessageWithAttachment(fileUri: Uri, messageText: String) {
        // Реализуйте отправку сообщения с файлом (например, загружайте файл на сервер)
        val currentUserID = Firebase.auth.currentUser?.uid
        val message = mapOf(
            "text" to messageText,
            "currentUserID" to currentUserID,
            "imageUri" to fileUri.toString(), // Передаем путь к файлу в сообщении
            "read" to false
        )
        database.reference.child("chats").child(chatId)
            .child("messages").push().setValue(message)
    }

    private fun chatWithUser(reference: DatabaseReference) {
        reference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages: MutableList<Message> = mutableListOf()
                for (i in snapshot.children) {
                    val messageId = i.key
                    val currentUserID = i.child("currentUserID").value.toString()
                    val text = i.child("text").value.toString()
                    val imageUri = i.child("imageUri").value?.toString()
                    val readMessage = i.child("read").getValue(Boolean::class.java) ?: false

                    val message =
                        messageId?.let { Message(it, currentUserID, text, imageUri, readMessage) }
                    if (message != null) {
                        messages.add(message)
                    }
                }

                adapter.updateMessages(messages)
                adapter.notifyDataSetChanged()

                binding.messagesRV.post {
                    binding.messagesRV.scrollToPosition(adapter.itemCount - 1)
                }

                val lastMessage = messages.lastOrNull()
                if (lastMessage != null) {
                    val currentUserId = Firebase.auth.currentUser?.uid
                    if (lastMessage.currentUserID != currentUserId) {
                        binding.isSendTV.visibility = View.INVISIBLE
                    } else {
                        binding.isSendTV.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMessage(message: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserID = currentUser?.uid

        if (currentUserID != null) {
            val messageInfo = mapOf(
                "text" to message,
                "currentUserID" to currentUserID
            )
            FirebaseDatabase.getInstance().reference.child("chats").child(chatId)
                .child("messages").push().setValue(messageInfo)

            val userId = getChatmateId(currentUserID.toString(), chatId)
            val userRef =
                userId?.let { FirebaseDatabase.getInstance().reference.child("users").child(it) }

            currentUserID.let { currentUserID ->
                FirebaseDatabase.getInstance().reference.child("users").child(currentUserID)
                    .child("lastMessage").setValue(message)
            }
            userRef?.child("lastMessage")?.setValue(message)

            userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val token = dataSnapshot.child("token").getValue(String::class.java)
                    Log.e("pushNotification", "token: $token")
                    /*if (token != null) {
                        pushNotification(message, token)
                    }*/
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("pushNotification", "User data token could not be uploaded: $error")
                }
            })


        } else {
            Log.e("sendMessage", "Error: currentUser or uid is null")
        }
    }

    private fun getChatmateId(currentUserId: String, chatId: String): String {
        val userIds = chatId.split("-")
        return if (userIds[0] == currentUserId) {
            userIds[1]
        } else {
            userIds[0]
        }
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    fun showImageDialog(imageUri: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_image, null)
        val imageView: ImageView = dialogView.findViewById(R.id.dialogIV)

        // Отображаем название картинки, если это строка, а не URI
        imageView.setImageDrawable(null) // Очистим изображение, если вдруг оно было установлено раньше
        imageView.setVisibility(View.GONE) // Скрываем ImageView, так как изображения нет

        val textView: TextView =
            dialogView.findViewById(R.id.imageDialogTV) // Предполагаем, что в layout есть TextView для текста
        textView.text = "Название изображения: $imageUri" // Отображаем название картинки

        builder.setView(dialogView)
            .setPositiveButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteMessage(message: Message) {
        database.reference.child("chats").child(chatId)
            .child("messages").child(message.id)
            .removeValue()
    }
}