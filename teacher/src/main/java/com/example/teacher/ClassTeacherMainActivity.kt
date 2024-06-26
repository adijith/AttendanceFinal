package com.example.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityClassTeacherMainBinding
import com.google.firebase.auth.FirebaseAuth

class ClassTeacherMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassTeacherMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassTeacherMainBinding.inflate(layoutInflater)
//
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is signed in
            val email = currentUser.email
            Toast.makeText(this, "Logged in as: $email", Toast.LENGTH_SHORT).show()
        } else {
            // No user is signed in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
        binding.studentOptionsButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, StudentOptionsActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceMarkingButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, AttenMarkActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceOptionsButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, AttendanceOptionsActivity::class.java)
            startActivity(intent)
        }

        binding.profileOptions.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, ProfileOptionsActivity::class.java)
            startActivity(intent)
        }
    binding.logout.setOnClickListener {

        firebaseAuth.signOut()

        val intent = Intent().apply {
            setClassName("com.example.attendancefinal", "com.example.attendancefinal.LoginActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }


    }
}