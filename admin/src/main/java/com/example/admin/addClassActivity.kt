package com.example.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivityAddClassBinding
import com.google.firebase.database.*

class addClassActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var teacherRef: DatabaseReference
    private lateinit var binding: ActivityAddClassBinding
    private lateinit var subjectsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        teacherRef = database.getReference("Teachers")
        subjectsRef = database.getReference("Subjects")

        binding.submitButton.setOnClickListener {
            val subject = binding.addClass.text.toString().trim()
            val selectedTeacherTriple = binding.addClassTeacher.selectedItem as Triple<String, String, String>

            if (subject.isNotEmpty()) {
                val attendanceRef = database.getReference("attendance")

                // Create a new child node under "students" with subject as the key
                val subjectRef = attendanceRef.child(subject)

                // Set the teacherUid as values under the subject node
                subjectRef.child("teacherUid").setValue(selectedTeacherTriple.third)
                teacherRef.child(selectedTeacherTriple.third).child("tclass").setValue(subject)

                subjectsRef.child(subject).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@addClassActivity,
                            "Subject added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Clear the EditText after submitting
                        binding.addClass.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@addClassActivity,
                            "Failed to add subject: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this@addClassActivity,
                    "Please enter a subject",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Populate spinner with teacher names and IDs
        populateSpinner(teacherRef)
    }

    private fun populateSpinner(teacherRef: DatabaseReference) {
        val teacherList = mutableListOf<Triple<String, String, String>>()

        teacherRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                teacherList.clear()
                for (snapshot in dataSnapshot.children) {
                    val teacherName = snapshot.child("fname").getValue(String::class.java)
                    val teacherId = snapshot.child("teacherId").getValue(String::class.java)
                    val teacherUid = snapshot.key ?: continue
                    teacherName?.let {
                        teacherId?.let { id ->
                            val teacherInfo = Triple(teacherName, teacherId, teacherUid)
                            teacherList.add(teacherInfo)
                        }
                    }
                }

                // Update spinner adapter with teacher names and IDs
                val adapter = ArrayAdapter(
                    this@addClassActivity,
                    android.R.layout.simple_spinner_item,
                    teacherList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.addClassTeacher.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(
                    this@addClassActivity,
                    "Failed to retrieve data from database.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}