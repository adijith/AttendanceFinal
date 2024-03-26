package com.example.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admin.databinding.ActivityAddTeacherBinding
import com.example.data.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddTeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTeacherBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Teacher information")
        progressBar = binding.progressBar

        binding.submitBtnTeacher.setOnClickListener {
            val email = binding.emailInputT.text.toString()
            val password = binding.passwordInputT.text.toString()
            val name = binding.teacherName.text.toString()
            val subject = binding.subjectTeachers.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = ProgressBar.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            saveTeacherToDatabase(user, name, subject)
                        } else {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTeacherToDatabase(user: FirebaseUser?, name: String, subject: String) {
        user?.let {
            val teacherId = user.uid
            val teacher = Teacher(user.email ?: "", name, subject)

            // Save teacher information
            databaseReference.child("Teacher information").child(teacherId).setValue(teacher)
                .addOnSuccessListener {
                    // Once teacher information is saved successfully, save role mapping
                    val userRole = UserRole("Teacher")
                    databaseReference.child("UserRoles").child(teacherId).setValue(userRole)
                        .addOnSuccessListener {
                            progressBar.visibility = ProgressBar.GONE
                            binding.teacherName.text.clear()
                            binding.emailInputT.text.clear()
                            binding.passwordInputT.text.clear()
                            binding.subjectTeachers.text.clear()
                            Toast.makeText(this, "Teacher saved successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AddTeacherActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { exception ->
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(this, "Failed to save teacher role: ${exception.message}", Toast.LENGTH_SHORT).show()
                            Log.e("AddTeacherActivity", "Failed to save teacher role", exception)
                        }
                }.addOnFailureListener { exception ->
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Failed to save teacher: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddTeacherActivity", "Failed to save teacher", exception)
                }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}