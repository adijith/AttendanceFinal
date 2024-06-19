package com.example.teacher

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class AddStudentActivity : AppCompatActivity() {

    private lateinit var studentIdEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        studentIdEditText = findViewById(R.id.student_id)
        emailEditText = findViewById(R.id.email_input_s)
        nameEditText = findViewById(R.id.student_name)
        submitButton = findViewById(R.id.submit_btn_student)
        progressBar = findViewById(R.id.progress_bar)

        databaseReference = FirebaseDatabase.getInstance().reference
        currentUser = FirebaseAuth.getInstance().currentUser!!

        submitButton.setOnClickListener {
            addStudent()
        }
    }

    private fun addStudent() {
        val studentId = studentIdEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val active = true // Set default value for active status

        if (studentId.isEmpty() || email.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        // Check for uniqueness of studentId
        databaseReference.child("Students").orderByChild("studentId").equalTo(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@AddStudentActivity, "Student ID already exists", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    } else {
                        // Check for uniqueness of email
                        databaseReference.child("Students").orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        Toast.makeText(this@AddStudentActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                                        progressBar.visibility = View.GONE
                                    } else {
                                        // Fetch tclass (sclass for students) from Firebase Database based on the current user's uid
                                        val userRef = databaseReference.child("Teachers").child(currentUser.uid)
                                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val sclass = snapshot.child("tclass").getValue(String::class.java) ?: ""
                                                    saveStudentToDatabase(studentId, email, name, sclass, active)
                                                } else {
                                                    Toast.makeText(this@AddStudentActivity, "Teacher data not found", Toast.LENGTH_SHORT).show()
                                                    progressBar.visibility = View.GONE
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(this@AddStudentActivity, "Failed to fetch teacher data", Toast.LENGTH_SHORT).show()
                                                progressBar.visibility = View.GONE
                                            }
                                        })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@AddStudentActivity, "Failed to check email uniqueness", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddStudentActivity, "Failed to check student ID uniqueness", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            })
    }

    private fun saveStudentToDatabase(studentId: String, email: String, name: String, sclass: String, active: Boolean) {
        val student = Student(studentId, email, name, sclass, active)

        // Generate a unique key for the new student
        val id = databaseReference.child("Students").push().key ?: ""

        // Create a map to hold the student data
        val studentValues = student.toMap()

        // Save the student data to Firebase
        databaseReference.child("Students").child(id).setValue(studentValues)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this@AddStudentActivity, "Student added", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(this@AddStudentActivity, "Failed to add student", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Extension function to convert Student object to a map
    private fun Student.toMap(): Map<String, Any?> {
        return mapOf(
            "studentId" to this.StudentId,
            "email" to this.email,
            "name" to this.name,
            "sclass" to this.sclass,
            "active" to this.active
        )
    }

    private fun clearForm() {
        studentIdEditText.text.clear()
        emailEditText.text.clear()
        nameEditText.text.clear()
    }
}
