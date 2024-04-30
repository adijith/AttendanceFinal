package com.example.admin

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivityAddSubjectBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddSubjectActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAddSubjectBinding // Assuming you have ActivityAddSubjectBinding
    private lateinit var subjectReference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectReference = FirebaseDatabase.getInstance().getReference("Subjects")
        progressBar = binding.progressBar  // Assuming you have a progress bar in the layout

        binding.addSubjectButton.setOnClickListener {
            val subjectId = binding.subjectIdEditText.text.toString().trim()  // Handle empty input
            val subjectName = binding.subjectNameEditText.text.toString().trim()
            val semester = binding.semesterEditText.text.toString().trim()
            val department = binding.departmentEditText.text.toString().trim()

            // Check if subject name is entered (required field)
            if (subjectName.isEmpty()) {
                binding.subjectNameEditText.error = "Subject name is required"
                return@setOnClickListener
            }

            // Firebase Database operations
            val newSubjectId = if (subjectId.isNotEmpty()) subjectId else subjectReference.push().key ?: return@setOnClickListener

            val subject = Subject(newSubjectId, subjectName, semester, department)
            subjectReference.child(newSubjectId).setValue(subject)
                .addOnSuccessListener {
                    progressBar.visibility = ProgressBar.GONE
                    binding.subjectIdEditText.text.clear()  // Clear subject ID field (optional)
                    binding.subjectNameEditText.text.clear()
                    binding.semesterEditText.text.clear()
                    binding.departmentEditText.text.clear()
                    Toast.makeText(this, "Subject saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Failed to save subject: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddSubjectActivity", "Failed to save subject", exception)
                }
        }
    }
}

