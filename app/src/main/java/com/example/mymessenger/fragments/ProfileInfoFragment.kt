package com.example.mymessenger.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymessenger.R
import com.example.mymessenger.databinding.FragmentProfileInfoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class ProfileInfoFragment : Fragment() {

    private var _binding: FragmentProfileInfoBinding? = null
    private val binding get() = _binding!!
    private var phone: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId").toString()

        val reference =
            userId.let { FirebaseDatabase.getInstance().reference.child("users").child(it) }
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val id = dataSnapshot.child("id").getValue(String::class.java )
                val name = dataSnapshot.child("name").getValue(String::class.java)
                val surname = dataSnapshot.child("surname").getValue(String::class.java)
                val role = dataSnapshot.child("role").getValue(String::class.java)
                val address = dataSnapshot.child("address").getValue(String::class.java)
                val age = dataSnapshot.child("age").getValue(String::class.java)
                val profileImageUri =
                    dataSnapshot.child("profileImageUri").getValue(String::class.java)
                val email = dataSnapshot.child("email").getValue(String::class.java)
                phone = dataSnapshot.child("phone").getValue(String::class.java)

                val emailMasked = email?.let {
                    val atIndex = it.indexOf('@')
                    val maskedPart = it.substring(0, atIndex - 2).replace(Regex("."), "*")
                    val lastPart = it.substring(atIndex - 2)
                    maskedPart + lastPart
                }

                binding.idTV.text = id
                binding.usernameTV.text = name
                binding.nameTV.text = name
                binding.surnameTV.text = surname
                binding.roleTV.text = role
                binding.addressTV.text = address
                binding.ageTV.text = age
                binding.phoneTV.text = phone
                binding.emailTV.text = emailMasked

                if (!profileImageUri.isNullOrEmpty()) {
                    Picasso.get()
                        .load(profileImageUri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.profileImageIV)
                } else {
                    Picasso.get()
                        .load(R.drawable.profile)
                        .into(binding.profileImageIV)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.toCallBTN.setOnClickListener {
            phone?.let { number ->
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }
                startActivity(callIntent)
            }
        }

        binding.senSmsBTN.setOnClickListener {
            phone?.let { number ->
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$number")
                }
                startActivity(smsIntent)
            }
        }
    }

}