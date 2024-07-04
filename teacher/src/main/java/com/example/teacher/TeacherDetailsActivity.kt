package com.example.teacher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityTeacherDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TeacherDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the current student ID
        val teacherId = firebaseAuth.currentUser?.uid

        // Set click listener for Save button
        binding.buttonSave.setOnClickListener {
            saveTeacherInfo(teacherId)
        }
    }

    private fun saveTeacherInfo(teacherId: String?) {
        if (teacherId != null) {
            val phone = binding.editTextPhone.text.toString().trim()
            val dob = binding.editTextDOB.text.toString().trim()
            val qualifications = binding.editTextQualification.text.toString().trim()
            val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
                R.id.radioButtonMale -> "Male"
                R.id.radioButtonFemale -> "Female"
                R.id.radioButtonOther -> "Other"
                else -> ""
            }

            // Validate input (optional)

            // Save data to Firebase
            val teacherRef = database.getReference("Teachers").child(teacherId)
            teacherRef.child("phone").setValue(phone)
            teacherRef.child("dob").setValue(dob)
            teacherRef.child("qualifications").setValue(qualifications)
            teacherRef.child("gender").setValue(gender)
                .addOnSuccessListener {
                    // Data successfully saved
                    Toast.makeText(this@TeacherDetailsActivity, "Teacher information saved successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close this activity after successful save
                }
                .addOnFailureListener {
                    // Handle error saving data
                    Toast.makeText(this@TeacherDetailsActivity, "Failed to save teacher information. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}