package com.example.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.student.databinding.ActivityStudentMainBinding
import com.google.firebase.auth.FirebaseAuth

class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for View Attendance button
        binding.viewAttendanceButton.setOnClickListener {
            // Start the ViewAttendanceActivity
            val intent = Intent(this@StudentMainActivity, ViewAttendanceActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            // Sign out user from Firebase Authentication
            firebaseAuth.signOut()

            val intent = Intent().apply {
                setClassName("com.example.attendancefinal", "com.example.attendancefinal.LoginActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // Finish current activity to prevent going back on logout
        }
    }
}