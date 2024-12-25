package com.example.mymessenger

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.mymessenger.databinding.ActivityMainBinding
import com.example.mymessenger.fragments.MyProfileFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_profile -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_Host_Fragment, MyProfileFragment())
                    .addToBackStack(null)
                    .commit()
                return true
            }

            R.id.menu_logout -> {
                Firebase.auth.signOut()
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
