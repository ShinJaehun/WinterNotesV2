package com.shinjaehun.winternotesv2.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shinjaehun.winternotesv2.R
import com.shinjaehun.winternotesv2.databinding.FragmentLoginBinding

private const val TAG = "LoginView"

class LoginView : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModel = ViewModelProvider(
            this,
            LoginInjector(requireActivity().application).provideLoginViewModelFactory()
        ).get(LoginViewModel::class.java)

        setUpClickListeners()
        observeViewModel()

//        binding.btnLogin.setOnClickListener {
//            findNavController().navigate(R.id.noteListView)
//        }

        viewModel.handleEvent(LoginEvent.OnStart)
    }

    private fun setUpClickListeners() {
        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = true
            viewModel.handleEvent(LoginEvent.OnLoginButtonClick(email,password))
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(
            viewLifecycleOwner,
            Observer {
//                findNavController().navigate(R.id.noteListView)
            }
        )
    }
}