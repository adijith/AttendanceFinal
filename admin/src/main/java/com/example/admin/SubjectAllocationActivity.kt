package com.example.admin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivitySubjectAllocationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SubjectAllocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectAllocationBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectAllocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().getReference("Subjects")

        val database = FirebaseDatabase.getInstance()
        val teacherRef = database.getReference("Teachers")

        teacherRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val teacherList = mutableListOf<Triple<String, String, String>>()
                for (snapshot in dataSnapshot.children) {
                    val isActive = snapshot.child("active").getValue(Boolean::class.java) ?: false
                    if (isActive) {
                        val teacherId = snapshot.child("teacherId").getValue(String::class.java)
                        val teacherName = snapshot.child("fname").getValue(String::class.java)
                        val teacherUid = snapshot.key ?: continue
                        teacherName?.let {
                            teacherId?.let { id ->
                                val teacherInfo = Triple(teacherName, teacherId, teacherUid)
                                teacherList.add(teacherInfo)
                            }
                        }
                    }
                }
                val adapter = ArrayAdapter(
                    this@SubjectAllocationActivity,
                    R.layout.spinner_item,
                    teacherList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner1.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })



        val subjectsRef = database.getReference("Subjects")

        subjectsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subjectList = mutableListOf<String>()

                for (snapshot in dataSnapshot.children) {
                    val subjectId = snapshot.key // Get the subject ID
                    if (subjectId != null) {
                        subjectList.add(subjectId)
                    }
                }

                val subjectIdAdapter = ArrayAdapter(
                    this@SubjectAllocationActivity,
                    R.layout.spinner_item,
                    subjectList
                )
                subjectIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner2.adapter = subjectIdAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(
                    this@SubjectAllocationActivity,
                    "Failed to retrieve data from database.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Button click listener
        binding.buttonAllocate.setOnClickListener {
            allocateSubject()
        }
    }

    private fun allocateSubject() {
        // Get values from views
        val subjectId = binding.SubjectID.text.toString().trim()
        val subjectName = binding.SubjectName.text.toString().trim()
        val selectedTeacherTriple = binding.spinner1.selectedItem as Triple<String, String, String>
        val teacherCode = selectedTeacherTriple.third

        val selectedSubjectId = binding.spinner2.selectedItem.toString()

        // Validate input
        if (subjectId.isEmpty() || subjectName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map with subject details
        val subjectDetails = mapOf(
            "subjectId" to subjectId,
            "subjectName" to subjectName,
            "tuid" to teacherCode
        )

        val subjectRef = databaseReference.child("$selectedSubjectId/$subjectId")
        subjectRef.setValue(subjectDetails)

        val rootReference = FirebaseDatabase.getInstance().reference
        val teacherSubjectsRef = rootReference.child("Teachers/$teacherCode/subjects/$subjectId")
        teacherSubjectsRef.setValue(selectedSubjectId)


        // Notify user
        Toast.makeText(this, "Subject allocated successfully", Toast.LENGTH_SHORT).show()
    }



}