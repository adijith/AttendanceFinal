package com.example.teacher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityTeacherProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TeacherProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var teacherRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the current student ID
        val teacherId = firebaseAuth.currentUser?.uid
        teacherId?.let {
            teacherRef = database.getReference("Teachers").child(it)
            fetchStudentInfo()
        }
    }

    private fun fetchStudentInfo() {
        teacherRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ensure the snapshot has data
                if (snapshot.exists()) {
                    val name = snapshot.child("fname").getValue(String::class.java)
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    val email = snapshot.child("emailt").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val qualifications = snapshot.child("qualifications").getValue(String::class.java)
                    val tclass = snapshot.child("tclass").getValue(String::class.java)
                    val studentId = snapshot.child("teacherId").getValue(String::class.java)

                    // Update UI with retrieved data
                    binding.nameTextView.text = "name: $name"
                    binding.genderTextView.text = "gender: $gender"
                    binding.emailTextView.text = "Email: $email"
                    binding.phoneTextView.text = "Phone: $phone"
                    binding.classTextView.text = "Class: $tclass"
                    binding.qualificationTextView.text = "Qualifications: $qualifications"
                    binding.roleNumberTextView.text = "Role Number: $studentId"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                // For simplicity, show a toast message
                Toast.makeText(this@TeacherProfileActivity, "Failed to retrieve student information", Toast.LENGTH_SHORT).show()
            }
        })
    }
}