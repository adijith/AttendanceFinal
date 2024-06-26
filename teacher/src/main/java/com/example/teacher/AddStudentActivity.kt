package com.example.teacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.data.UserRole
import com.example.teacher.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var studentReference: DatabaseReference
    private lateinit var userRoleReference: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var tclass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userRoleReference = FirebaseDatabase.getInstance().getReference("UserRoles")
        progressBar = binding.progressBar

        val teacherUid = firebaseAuth.currentUser!!.uid

        // Fetch tclass here
        val teachersReference = FirebaseDatabase.getInstance().getReference("Teachers")
        teachersReference.child(teacherUid).child("tclass").get()
            .addOnSuccessListener { classSnapshot ->
                tclass = classSnapshot.getValue(String::class.java) ?: ""
                if (tclass.isEmpty()) {
                    Log.e("AddStudentActivity", "No class found for teacher $teacherUid.")
                }
            }.addOnFailureListener { exception ->
                progressBar.visibility = ProgressBar.GONE
                Log.e(
                    "AddStudentActivity",
                    "Failed to get tclass role: ${exception.message}",
                    exception
                )
                Toast.makeText(
                    this,
                    "Failed to add tclass role: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        binding.submitBtnStudent.setOnClickListener {
            val studentId = binding.studentId.text.toString().trim()
            val email = binding.emailInputS.text.toString()
            val password = binding.studentPass.text.toString()
            val name = binding.studentName.text.toString()

            if (studentId.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && tclass.isNotEmpty()) {
                progressBar.visibility = ProgressBar.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val student = Student(studentId, user?.email ?: "", name, tclass)

                            val studentReference =
                                FirebaseDatabase.getInstance().getReference("Students").child(user?.uid ?: "")
                            studentReference.setValue(student)
                                .addOnSuccessListener {
                                    val userRole = UserRole("Student")
                                    val userRoleReference =
                                        FirebaseDatabase.getInstance().getReference("UserRoles")
                                    userRoleReference.child(user?.uid ?: "")
                                        .setValue(userRole)
                                        .addOnSuccessListener {
                                            handleAttendance(user!!, tclass)
                                        }
                                        .addOnFailureListener { exception ->
                                            progressBar.visibility = ProgressBar.GONE
                                            Toast.makeText(
                                                this,
                                                "Failed to save Student role: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e(
                                                "AddStudentActivity",
                                                "Failed to save Student role",
                                                exception
                                            )
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    progressBar.visibility = ProgressBar.GONE
                                    Toast.makeText(
                                        this,
                                        "Failed to add student ${user?.uid}: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e(
                                        "AddStudentActivity",
                                        "Failed to add student ${user?.uid}: ${exception.message}"
                                    )
                                }
                        } else {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(
                                this,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAttendance(user: FirebaseUser, tclass: String) {
        val attendanceReference =
            FirebaseDatabase.getInstance().getReference("attendance").child(tclass)
        attendanceReference.child(user.uid).setValue(true).addOnSuccessListener {
            Log.d("AddStudentActivity", "Attendance added for user: ${user.uid}")
            val subjectsReference =
                FirebaseDatabase.getInstance().getReference("Subjects").child(tclass)
            subjectsReference.get().addOnSuccessListener { subjectsSnapshot ->
                Log.d("AddStudentActivity", "Subjects Snapshot: $subjectsSnapshot")
                for (subjectSnapshot in subjectsSnapshot.children) {
                    val subjectId = subjectSnapshot.key
                    subjectId?.let { id ->
                        attendanceReference.child(user.uid).child(id).setValue(true)
                            .addOnSuccessListener {
                                progressBar.visibility = ProgressBar.GONE
                                binding.studentId.text.clear()  // Clear teacher ID textbox
                                binding.emailInputS.text.clear()
                                binding.studentPass.text.clear()
                                binding.studentName.text.clear()
                                Toast.makeText(this, "Teacher saved successfully", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this@AddStudentActivity, ClassTeacherMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                progressBar.visibility = ProgressBar.GONE
                                Log.e(
                                    "AddStudentActivity",
                                    "Failed to save Student ",
                                    exception
                                )
                                Toast.makeText(
                                    this,
                                    "Failed to save Student role: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }.addOnFailureListener { exception ->
                progressBar.visibility = ProgressBar.GONE
                Log.e(
                    "AddStudentActivity",
                    "Failed to save Student subject: ${exception.message}",
                    exception
                )
                Toast.makeText(
                    this,
                    "Failed to save Student role: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            progressBar.visibility = ProgressBar.GONE
            Log.e(
                "AddStudentActivity",
                "Failed to add attendance role: ${exception.message}",
                exception
            )
            Toast.makeText(
                this,
                "Failed to save Student role: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}