package com.example.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
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
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var teacherReference: DatabaseReference
    private lateinit var userRoleReference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userRoleReference = FirebaseDatabase.getInstance().getReference("UserRoles")
        progressBar = binding.progressBar

        binding.submitBtnTeacher.setOnClickListener {
            val teacherId = binding.teacherId.text.toString().trim() // Get the teacher ID from the textbox
            val email = binding.emailInputT.text.toString()
            val password = binding.passwordInputT.text.toString()
            val name = binding.teacherName.text.toString()

            if (teacherId.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = ProgressBar.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val Tclass = "none" // Default value for Tclass
                            saveTeacherToDatabase(user!!, teacherId, name, Tclass)
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


    private fun saveTeacherToDatabase(
        user: FirebaseUser,
        teacherId: String,
        name: String,
        Tclass: String // Tclass parameter to receive the value
    ) {
        if (teacherId.isEmpty()) {
            Toast.makeText(this, "Please enter a teacher ID", Toast.LENGTH_SHORT).show()
            return
        }

        val teacher = Teacher(teacherId, user.email ?: "", name, Tclass)

        // Save teacher information under Teachers node with the provided teacherId
        val teacherReference =
            FirebaseDatabase.getInstance().getReference("Teachers").child(user.uid)
        teacherReference.setValue(teacher)
        teacherReference.child("subs").setValue(true)
            .addOnSuccessListener {
                // Save user role information with Firebase Auth ID
                val userRole = UserRole("Teacher")
                userRoleReference.child(user.uid).setValue(userRole)
                    .addOnSuccessListener {
                        progressBar.visibility = ProgressBar.GONE
                        binding.teacherId.text.clear()  // Clear teacher ID textbox
                        binding.emailInputT.text.clear()
                        binding.passwordInputT.text.clear()
                        binding.teacherName.text.clear()
                        Toast.makeText(this, "Teacher saved successfully", Toast.LENGTH_SHORT)
                            .show()



                        val intent = Intent(this@AddTeacherActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(
                            this,
                            "Failed to save teacher role: ${exception.message}", Toast.LENGTH_SHORT
                        ).show()
                        Log.e("AddTeacherActivity", "Failed to save teacher role", exception)
                    }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(
                    this,
                    "Failed to save teacher: ${exception.message}", Toast.LENGTH_SHORT
                ).show()
                Log.e("AddTeacherActivity", "Failed to save teacher", exception)
            }
    }
}