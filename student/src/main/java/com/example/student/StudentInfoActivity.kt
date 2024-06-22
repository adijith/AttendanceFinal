package com.example.student

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.student.databinding.ActivityStudentInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class StudentInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the current student ID
        val studentId = firebaseAuth.currentUser?.uid

        // Set click listener for Save button
        binding.buttonSave.setOnClickListener {
            saveStudentInfo(studentId)
        }
    }

    private fun saveStudentInfo(studentId: String?) {
        if (studentId != null) {
            val phone = binding.editTextPhone.text.toString().trim()
            val dob = binding.editTextDOB.text.toString().trim()
            val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
                R.id.radioButtonMale -> "Male"
                R.id.radioButtonFemale -> "Female"
                R.id.radioButtonOther -> "Other"
                else -> ""
            }

            // Validate input (optional)

            // Save data to Firebase
            val studentsRef = database.getReference("Students").child(studentId)
            studentsRef.child("phone").setValue(phone)
            studentsRef.child("dob").setValue(dob)
            studentsRef.child("gender").setValue(gender)
                .addOnSuccessListener {
                    // Data successfully saved
                    Toast.makeText(this@StudentInfoActivity, "Student information saved successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close this activity after successful save
                }
                .addOnFailureListener {
                    // Handle error saving data
                    Toast.makeText(this@StudentInfoActivity, "Failed to save student information. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}