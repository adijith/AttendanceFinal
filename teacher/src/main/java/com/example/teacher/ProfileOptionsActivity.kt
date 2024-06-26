package com.example.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityProfileOptionsBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileOptionsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for View Attendance button
        binding.TeacherProfile.setOnClickListener {
            val intent = Intent(this@ProfileOptionsActivity, TeacherProfileActivity::class.java)
            startActivity(intent)
        }
        binding.TeacherDetails.setOnClickListener {
            val intent = Intent(this@ProfileOptionsActivity, TeacherDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}
