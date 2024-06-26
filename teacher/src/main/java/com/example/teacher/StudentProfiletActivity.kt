package com.example.teacher

import com.example.teacher.databinding.ActivityStudentProfiletBinding
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentProfiletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfiletBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var studentRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfiletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the student ID from the intent
        val studentId = intent.getStringExtra("STUDENT_ID")

        if (studentId != null) {
            studentRef = database.getReference("Students").child(studentId)
            fetchStudentInfo()
        } else {
            Toast.makeText(this, "No student ID provided.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStudentInfo() {
        studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ensure the snapshot has data
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val sclass = snapshot.child("sclass").getValue(String::class.java)
                    val rollNumber = snapshot.child("rollNumber").getValue(String::class.java)

                    // Update UI with retrieved data
                    binding.nameTextView.text = "Name: $name"
                    binding.genderTextView.text = "Gender: $gender"
                    binding.emailTextView.text = "Email: $email"
                    binding.phoneTextView.text = "Phone: $phone"
                    binding.classTextView.text = "Class: $sclass"
                    binding.roleNumberTextView.text = "Roll Number: $rollNumber"
                } else {
                    Toast.makeText(this@StudentProfiletActivity, "Student data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(this@StudentProfiletActivity, "Failed to retrieve student information", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
