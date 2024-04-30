package com.example.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.logout.setOnClickListener {
            val intent = Intent(this@MainActivity, AddTeacherActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.addSubjectButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddSubjectActivity::class.java)
            startActivity(intent)
        }
    }
}