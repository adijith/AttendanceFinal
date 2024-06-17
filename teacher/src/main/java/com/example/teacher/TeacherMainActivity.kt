package com.example.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityClassTeacherMainBinding
import com.google.firebase.auth.FirebaseAuth

class TeacherMainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityClassTeacherMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassTeacherMainBinding.inflate(layoutInflater)
//
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.attendanceMarkingButton.setOnClickListener {
            val intent = Intent(this@TeacherMainActivity, AttenMarkActivity::class.java)
            startActivity(intent)
        }
    }
}