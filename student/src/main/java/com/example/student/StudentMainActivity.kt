package com.example.student

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // Set click listener for Modify Profile button
        binding.modifyProfileButton.setOnClickListener {
            // Start the ModifyProfileActivity
            val intent = Intent(this@StudentMainActivity, StudentInfoActivity::class.java)
            startActivity(intent)
        }
    }
}