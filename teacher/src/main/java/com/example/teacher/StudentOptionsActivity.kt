package com.example.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityStudentOptionsBinding
import com.google.firebase.auth.FirebaseAuth

class StudentOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentOptionsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for View Attendance button
        binding.addStudent.setOnClickListener {
            val intent = Intent(this@StudentOptionsActivity, AddStudentActivity::class.java)
            startActivity(intent)
        }
        binding.viewStudents.setOnClickListener {
            val intent = Intent(this@StudentOptionsActivity, ViewStudentsActivity::class.java)
            startActivity(intent)
        }
    }
}