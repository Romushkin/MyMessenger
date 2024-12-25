package com.example.mymessenger.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.ForgotPasswordViewModel
import com.example.mymessenger.R
import com.example.mymessenger.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetPasswordResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Инструкция по сбросу пароля отправлена на ваш email", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
            }.onFailure {
                Toast.makeText(requireContext(), "Ошибка при отправке письма для сброса пароля", Toast.LENGTH_SHORT).show()
            }
        })

        binding.sendForgotEmailBTN.setOnClickListener {
            val email = binding.emailForgotET.text.toString()

            if (email.isNotEmpty() && viewModel.isValidEmail(email)) {
                viewModel.resetPassword(email)
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, введите корректный email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}