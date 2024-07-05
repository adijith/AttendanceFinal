package com.example.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityAttendanceOptionsBinding
import com.google.firebase.auth.FirebaseAuth

class AttendanceOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceOptionsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for View Attendance button
        binding.attendanceViewingButton.setOnClickListener {
            val intent = Intent(this@AttendanceOptionsActivity, AttenViewActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceViewingButton2.setOnClickListener {
            val intent = Intent(this@AttendanceOptionsActivity, SubAttenViewActivity::class.java)
            startActivity(intent)
        }

        binding.attendanceEditButton.setOnClickListener {
            val intent = Intent(this@AttendanceOptionsActivity, AttenEditActivity::class.java)
            startActivity(intent)
        }
    }
}