package com.example.mymessenger.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.mymessenger.adapters.TabsAdapter
import com.example.mymessenger.models.Tabs.Companion.tabs
import com.example.mymessenger.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitleFromFirebase()
        val adapter = TabsAdapter(this, tabs)
        binding.mainViewPagerVP.adapter = adapter
        TabLayoutMediator(binding.mainTabLayoutTL, binding.mainViewPagerVP) { tab, position ->
            tab.text = tabs[position].name
        }.attach()
    }

   override fun onStart() {
        super.onStart()
        Firebase.auth.currentUser?.let { it1 ->
            Firebase.database.getReference("users").child(it1.uid)
                .child("isOnline").setValue(true)
        }
    }
    override fun onPause() {
        super.onPause()
        Firebase.auth.currentUser?.let {
            Firebase.database.getReference("users").child(it.uid)
                .child("isOnline").setValue(false)
        }
    }

    private fun setTitleFromFirebase() {
        val reference = Firebase.database.getReference("users")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentId = Firebase.auth.currentUser?.uid.toString()
                val email = snapshot.child(currentId).child("email").value.toString()
                val name = snapshot.child(currentId).child("name").value.toString()
                if (name == "") {
                    binding.titleNameTV.text = email
                } else binding.titleNameTV.text = name
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

}