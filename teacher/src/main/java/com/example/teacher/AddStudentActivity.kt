package com.example.teacher

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.data.UserRole
import com.example.teacher.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var tclass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
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
                handleFailure("Failed to get tclass", exception)
            }

        binding.submitBtnStudent.setOnClickListener {
            val studentId = binding.studentId.text.toString().trim()
            val email = binding.emailInputS.text.toString()
            val password = binding.studentPass.text.toString()
            val name = binding.studentName.text.toString()

            if (studentId.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && tclass.isNotEmpty()) {
                progressBar.visibility = ProgressBar.VISIBLE
                addStudent(studentId, email, password, name)
            } else {
                Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addStudent(studentId: String, email: String, password: String, name: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val studentUser = task.result?.user
                    if (studentUser != null) {
                        val student = Student(studentId, email, name, tclass)
                        saveStudentData(studentUser.uid, student)
                    } else {
                        handleFailure("Failed to create student account", null)
                    }
                } else {
                    handleFailure("Authentication failed", task.exception)
                }
            }
    }

    private fun saveStudentData(studentUid: String, student: Student) {
        val studentReference = FirebaseDatabase.getInstance().getReference("Students").child(studentUid)
        studentReference.setValue(student)
            .addOnSuccessListener {
                val userRole = UserRole("Student")
                val userRoleReference = FirebaseDatabase.getInstance().getReference("UserRoles")
                userRoleReference.child(studentUid).setValue(userRole)
                    .addOnSuccessListener {
                        handleAttendance(studentUid, tclass)
                    }
                    .addOnFailureListener { exception ->
                        handleFailure("Failed to save Student role", exception)
                    }
            }
            .addOnFailureListener { exception ->
                handleFailure("Failed to add student to database", exception)
            }
    }

    private fun handleAttendance(studentUid: String, tclass: String) {
        val attendanceReference = FirebaseDatabase.getInstance().getReference("attendance").child(tclass)
        attendanceReference.child(studentUid).setValue(true).addOnSuccessListener {
            Log.d("AddStudentActivity", "Attendance added for user: $studentUid")
            val subjectsReference = FirebaseDatabase.getInstance().getReference("Subjects").child(tclass)
            subjectsReference.get().addOnSuccessListener { subjectsSnapshot ->
                Log.d("AddStudentActivity", "Subjects Snapshot: $subjectsSnapshot")
                if (subjectsSnapshot.hasChildren()) {
                    for (subjectSnapshot in subjectsSnapshot.children) {
                        val subjectId = subjectSnapshot.key
                        subjectId?.let { id ->
                            attendanceReference.child(studentUid).child(id).setValue(true)
                                .addOnSuccessListener {
                                    Log.d("AddStudentActivity", "Subject attendance added for: $id")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("AddStudentActivity", "Failed to add subject attendance for: $id", exception)
                                }
                        }
                    }
                } else {
                    Log.d("AddStudentActivity", "No subjects found for class: $tclass")
                }
                finishStudentAddition()
            }.addOnFailureListener { exception ->
                handleFailure("Failed to fetch subjects", exception)
            }
        }.addOnFailureListener { exception ->
            handleFailure("Failed to add attendance", exception)
        }
    }

    private fun finishStudentAddition() {
        progressBar.visibility = ProgressBar.GONE
        showTeacherAuthDialog(binding.studentName.text.toString())
    }

    private fun showTeacherAuthDialog(studentName: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_teacher_auth, null)

        val emailEditText = dialogView.findViewById<EditText>(R.id.teacherEmailEditText)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.teacherPasswordEditText)

        builder.setView(dialogView)
            .setTitle("Re-authenticate to add \"$studentName\"")
            .setPositiveButton("Authenticate") { _, _ ->
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                authenticateTeacher(email, password)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                finish() // Return to previous activity if cancelled
            }

        builder.create().show()
    }

    private fun authenticateTeacher(email: String, password: String) {
        progressBar.visibility = ProgressBar.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    clearForm()
                    Toast.makeText(this, "Re-authenticated successfully. You can add another student.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous activity if authentication fails
                }
            }
    }

    private fun clearForm() {
        binding.studentId.text.clear()
        binding.emailInputS.text.clear()
        binding.studentPass.text.clear()
        binding.studentName.text.clear()
    }

    private fun handleFailure(message: String, exception: Exception?) {
        progressBar.visibility = ProgressBar.GONE
        Log.e("AddStudentActivity", "$message: ${exception?.message}", exception)
        Toast.makeText(this, "$message: ${exception?.message}", Toast.LENGTH_SHORT).show()
    }
}
