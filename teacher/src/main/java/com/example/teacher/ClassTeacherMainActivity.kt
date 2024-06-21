package com.example.teacher

import android.content.Intent
import android.os.Bundle
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
        binding.attendanceViewingButton2.setOnClickListener {
            val intent = Intent(this@ClassTeacherMainActivity, SubAttenViewActivity::class.java)
            startActivity(intent)
        }

    }
}