package com.example.profile_ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.profile_ui.databinding.FragmentSettingsBinding
import com.example.utils.CommonFun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initView()
        updateFragment()
        return binding.root
    }

    private fun updateFragment() {
        val userEmail = auth.currentUser?.email
        binding.tvEmail.text = userEmail
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            CommonFun.deepLinkNav("introFragment",requireActivity())
        }
    }
}