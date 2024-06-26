package com.example.teacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityViewStudentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewStudentsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var tclass: String // Class assigned to the teacher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the current teacher ID
        val teacherId = firebaseAuth.currentUser?.uid

        if (teacherId != null) {
            // Fetch the class assigned to the teacher
            val teacherRef = database.reference.child("Teachers").child(teacherId)
            teacherRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    tclass = dataSnapshot.child("tclass").getValue(String::class.java) ?: ""

                    // Set the heading text with the class name
                    binding.textViewClassHeading.text = "$tclass Students"

                    // Fetch the student details based on the class
                    fetchStudentDetails()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("ViewStudentsActivity", "Error fetching teacher class: ${databaseError.message}")
                    Toast.makeText(this@ViewStudentsActivity, "Error fetching class details.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStudentDetails() {
        // Reference to the attendance node for the specific class
        val attendanceRef = database.reference.child("attendance").child(tclass)

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (studentSnapshot in dataSnapshot.children) {
                    val studentId = studentSnapshot.key
                    if (studentId != null) {
                        fetchStudentInfo(studentId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViewStudentsActivity", "Error fetching attendance details: ${databaseError.message}")
                Toast.makeText(this@ViewStudentsActivity, "Error fetching student details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStudentInfo(studentId: String) {
        // Reference to the Students node for the specific student
        val studentRef = database.reference.child("Students").child(studentId)

        studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentName = dataSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val studentRollNumber = dataSnapshot.child("studentId").getValue(String::class.java) ?: "N/A"

                // Add student details to the table
                addStudentRow(studentRollNumber, studentName, studentId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViewStudentsActivity", "Error fetching student info: ${databaseError.message}")
                Toast.makeText(this@ViewStudentsActivity, "Error fetching student info.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addStudentRow(rollNumber: String, name: String, studentId: String) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // Roll Number
        val textViewRollNumber = TextView(this)
        textViewRollNumber.text = rollNumber
        textViewRollNumber.setPadding(16, 16, 16, 16)
        tableRow.addView(textViewRollNumber)

        // Student Name
        val textViewName = TextView(this)
        textViewName.text = name
        textViewName.setPadding(16, 16, 16, 16)
        tableRow.addView(textViewName)

        // View Profile Button
        val buttonViewProfile = Button(this)
        buttonViewProfile.text = "View Profile"
        buttonViewProfile.setPadding(16, 16, 16, 16)
        buttonViewProfile.setOnClickListener {
            // Handle view profile click
            val intent = Intent(this, StudentProfiletActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }
        tableRow.addView(buttonViewProfile)

        // Add the row to the table
        binding.tableLayoutStudents.addView(tableRow)
    }
}
