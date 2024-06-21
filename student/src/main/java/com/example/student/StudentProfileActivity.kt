package com.example.student

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.student.databinding.ActivityStudentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var studentRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the current student ID
        val studentId = firebaseAuth.currentUser?.uid
        studentId?.let {
            studentRef = database.getReference("Students").child(it)
            fetchStudentInfo()
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
                    val studentId = snapshot.child("studentId").getValue(String::class.java)

                    // Update UI with retrieved data
                    binding.nameTextView.text = "Name: $name"
                    binding.genderTextView.text = "Gender: $gender"
                    binding.emailTextView.text = "Email: $email"
                    binding.phoneTextView.text = "Phone: $phone"
                    binding.classTextView.text = "Class: $sclass"
                    binding.roleNumberTextView.text = "Role Number: $studentId"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                // For simplicity, show a toast message
                Toast.makeText(this@StudentProfileActivity, "Failed to retrieve student information", Toast.LENGTH_SHORT).show()
            }
        })
    }
}