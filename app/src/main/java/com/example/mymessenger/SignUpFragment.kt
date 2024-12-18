package com.example.mymessenger

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.databinding.FragmentFirstBinding
import com.example.mymessenger.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUpBTN.setOnClickListener {
            auth = Firebase.auth
            signUpUser()
        }
        binding.redirectLoginTV.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun signUpUser() {
        val email = binding.emailRegisterET.text.toString()
        val password = binding.passwordRegisterET.text.toString()
        val confirmPass = binding.passwordConfirmET.text.toString()

        if (email.isBlank() || password.isBlank() || confirmPass.isBlank()) {
            Toast.makeText(
                context,
                "Адрес электронной почты и пароль не могут быть пустыми",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password != confirmPass) {
            Toast.makeText(
                context,
                "Пароли не совпадают",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful ) {
                Toast.makeText(
                    context,
                    "Вы успешно зарегистрировались",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            } else {
                if (auth.currentUser != null) {
                    Toast.makeText(
                        context,
                        "Пользователь уже существует",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Toast.makeText(
                    context,
                    "Регистрация не прошла",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}