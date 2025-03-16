package com.example.authentication.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.authentication.R
import com.example.authentication.databinding.FragmentIntroBinding
import com.example.utils.CommonFun.applyScaleAnimation


class IntroFragment : Fragment() {
    private var _binding: FragmentIntroBinding? =null
    private val binding: FragmentIntroBinding
        get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroBinding.inflate(inflater, container, false)

        initView()
        return binding.root
    }

    private fun initView() {
        binding.btnSignup.applyScaleAnimation()
        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_signupFragment)
        }
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
        }
    }
}