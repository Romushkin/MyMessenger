package com.example.mymessenger

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        insertLottie()

        auth = FirebaseAuth.getInstance()

        binding.signInBTN.setOnClickListener {
            signInUser()
        }

        binding.signUpTV.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.forgotPasswordTV.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

    }

    private fun signInUser() {
        val email = binding.emailSignInET.text.toString()
        val password = binding.passwordSignInET.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    "Вы успешно вошли в систему",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Не удалось войти в систему",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun insertLottie() {
        val dotLottieAnimationView = binding.lottieUser
        val config = Config.Builder()
            .autoplay(true)
            .speed(1f)
            .source(DotLottieSource.Asset("user.lottie"))
            .build()
        dotLottieAnimationView.load(config)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}