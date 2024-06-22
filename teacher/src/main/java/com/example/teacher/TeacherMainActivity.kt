package com.example.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityTeacherMainBinding
import com.google.firebase.auth.FirebaseAuth

class TeacherMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMainBinding.inflate(layoutInflater)
//
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.attendanceMarkingButton.setOnClickListener {
            val intent = Intent(this@TeacherMainActivity, AttenMarkActivity::class.java)
            startActivity(intent)
        }
        binding.attendanceViewingButton2.setOnClickListener {
            val intent = Intent(this@TeacherMainActivity, SubAttenViewActivity::class.java)
            startActivity(intent)
        }
        binding.logout.setOnClickListener {
            firebaseAuth.signOut()

            val intent = Intent().apply {
                setClassName(
                    "com.example.attendancefinal",
                    "com.example.attendancefinal.LoginActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}