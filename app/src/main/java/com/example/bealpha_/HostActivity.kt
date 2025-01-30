package com.example.bealpha_

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bealpha_.databinding.ActivityHostBinding
import com.google.firebase.auth.FirebaseAuth

class HostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        binding.btnLogout.setOnClickListener {
           auth.signOut()

            Log.d("Logout", "User logged out")

            startActivity(Intent(this, MainActivity::class.java))
            finish()  // Close the HostActivity
        }
    }
}