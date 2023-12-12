package com.example.message.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.message.R
import com.example.message.databinding.FragmentLoginBinding
import com.example.message.viewmodel.LoginViewModel
import com.example.message.viewmodel.LoginViewModelFactory

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by activityViewModels {
        LoginViewModelFactory()
    }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeFragment = this@LoginFragment

        viewModel.loginResult.observe(this.viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.login_failure_notification),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun onLoginButton() {
        val email = binding.username.text.toString().trim()
        val password = binding.password.text.toString().trim()
        viewModel.login(email, password)
    }
}