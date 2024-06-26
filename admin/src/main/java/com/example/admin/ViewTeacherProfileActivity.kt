package com.example.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivityViewTeacherProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewTeacherProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTeacherProfileBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var teacherRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTeacherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()

        // Get the teacher ID from the intent
        val teacherId = intent.getStringExtra("TEACHER_ID")

        if (teacherId != null) {
            teacherRef = database.getReference("Teachers").child(teacherId)
            fetchTeacherInfo()
        } else {
            Toast.makeText(this, "No teacher ID provided.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTeacherInfo() {
        teacherRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                } else {
                    Toast.makeText(this@ViewTeacherProfileActivity, "Teacher data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewTeacherProfileActivity, "Failed to retrieve teacher information", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
