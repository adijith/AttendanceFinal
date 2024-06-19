package com.example.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityClassTeacherMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.teacher.AddStudentActivity

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
        binding.addStudentButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, AddStudentActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceMarkingButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, AttenMarkActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceViewingButton.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, AttenViewActivity::class.java)
            startActivity(intent)
        }
    }
}