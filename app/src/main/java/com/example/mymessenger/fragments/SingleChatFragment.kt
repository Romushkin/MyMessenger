package com.example.mymessenger.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymessenger.R
import com.example.mymessenger.RegistrationActivity
import com.example.mymessenger.adapters.SingleChatMessageAdapter
import com.example.mymessenger.databinding.FragmentSingleChatBinding
import com.example.mymessenger.models.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.squareup.picasso.Picasso
import java.lang.Exception


class SingleChatFragment : Fragment() {

    private var _binding: FragmentSingleChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SingleChatMessageAdapter
    private val database = Firebase.database
    private val auth = Firebase.auth
    private lateinit var chatId: String
    private var selectedFileUri: Uri? = null
    private var isFileSelected: Boolean = false
    private var REQUEST_SELECT_FILE = 302


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
        val profileImageUri = arguments?.getString("profileImageUri").toString()

        if (!profileImageUri.isNullOrEmpty()) {
            Picasso.get()
                .load(profileImageUri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        val drawable = BitmapDrawable(resources, bitmap)
                        binding.chatImageIV.setImageDrawable(drawable)
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        Log.e("Picasso", "Failed to load image: $e")
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        } else {
            Picasso.get()
                .load(R.drawable.ic_person)
                .into(binding.chatImageIV)
        }

        binding.chatmateInfoLL.setOnClickListener{
            val userId = Firebase.auth.currentUser?.let { it1 -> getChatmateId(it1.uid, chatId) }
            val bundle = Bundle()
            bundle.putString("userId", userId)
            findNavController().navigate(R.id.action_singleChatFragment_to_profileInfoFragment, bundle)
        }

        binding.sendMessageIB.setOnClickListener {
            if (isFileSelected) {
                selectedFileUri?.let {
                    sendMessageWithAttachment(it, binding.editMessageET.text.toString())

                    binding.attachImageIB.setImageResource(R.drawable.ic_attach_file)
                    selectedFileUri = null
                    isFileSelected = false
                    binding.editMessageET.text.clear()
                }
            } else {
                val messageText = binding.editMessageET.text.toString()
                if (messageText.isNotEmpty()) {
                    sendMessage(messageText)

                    binding.editMessageET.text.clear()
                } else {
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
        onChangeListener(
            database.reference.child("chats")
                .child(chatId).child("messages")
        )

        binding.attachImageIB.setOnClickListener {
            openGalleryForFile()
        }

    }

    private fun openGalleryForFile() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedFileUri = data.data
            isFileSelected = true
            binding.attachImageIB.setImageResource(R.drawable.ic_check)
        }
    }

    private fun sendMessageWithAttachment(fileUri: Uri, messageText: String) {
        val currentUserID = Firebase.auth.currentUser?.uid
        val message = mapOf(
            "text" to messageText,
            "currentUserID" to currentUserID,
            "imageUri" to fileUri.toString(),
            "read" to false
        )
        database.reference.child("chats").child(chatId)
            .child("messages").push().setValue(message)
    }

    private fun onChangeListener(reference: DatabaseReference) {
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
                        Handler().postDelayed({
                            binding.isSendTV.visibility = View.GONE
                        }, 2000)
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
                userId.let { FirebaseDatabase.getInstance().reference.child("users").child(it) }

            currentUserID.let { currentUserID ->
                FirebaseDatabase.getInstance().reference.child("users").child(currentUserID)
                    .child("lastMessage").setValue(message)
            }
            userRef.child("lastMessage").setValue(message)


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

        imageView.setImageDrawable(null)
        imageView.setVisibility(View.GONE)

        val textView: TextView =
            dialogView.findViewById(R.id.imageDialogTV)
        textView.text = "Название изображения: $imageUri"

        builder.setView(dialogView)
            .setPositiveButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteMessage(message: Message) {
        database.reference.child("chats").child(chatId)
            .child("messages").child(message.id).removeValue()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_single_chat, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_chatmate_profile -> {
                val userId = Firebase.auth.currentUser?.let { it1 -> getChatmateId(it1.uid, chatId) }
                val bundle = Bundle()
                bundle.putString("userId", userId)
                findNavController().navigate(R.id.action_singleChatFragment_to_profileInfoFragment, bundle)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}