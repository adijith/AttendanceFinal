package com.example.attendancefinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendancefinal.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.admin.MainActivity
import com.example.student.StudentMainActivity
import com.example.teacher.ClassTeacherMainActivity
import com.example.teacher.TeacherMainActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val loginUsername = binding.emailEditText.text.toString()
            val loginpassword = binding.passwordEditText.text.toString()

            if (loginUsername.isNotEmpty() && loginpassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(loginUsername, loginpassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            val currentUser = firebaseAuth.currentUser
                            if (currentUser != null) {
                                val uid = currentUser.uid

                                // Check user role in Firebase Realtime Database
                                val databaseReference = FirebaseDatabase.getInstance().getReference("UserRoles")
                                databaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
                                        if (dataSnapshot.exists()) {
                                            val role = dataSnapshot.child("uid").getValue(String::class.java)

                                            if (role == "Teacher") {
                                                val teachersReference =
                                                    FirebaseDatabase.getInstance()
                                                        .getReference("Teachers")
                                                teachersReference.child(uid).child("tclass").get()
                                                    .addOnSuccessListener { classSnapshot ->
                                                        if (classSnapshot.exists()) {
                                                            val tclass =
                                                                classSnapshot.getValue(String::class.java)
                                                            if (tclass != "none") {
                                                                // If the teacher has a class, start the corresponding activity
                                                                val intent = Intent(
                                                                    this,
                                                                    ClassTeacherMainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                            } else {
                                                                val intent = Intent(
                                                                    this,
                                                                    TeacherMainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                            }
                                                        }
                                                    }
                                            } else if (role == "Student") {
                                                val intent = Intent(this, StudentMainActivity::class.java)
                                                startActivity(intent)
                                            } else if (role == "Admin") {
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                            } else if (role == null) {
                                                // Handle invalid role or no role found
                                                Toast.makeText(this, "Invalid role or role not found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, "login failed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "all fields mandatory", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@LoginActivity, "all fields mandatory", Toast.LENGTH_SHORT).show()
            }
        }



            }
        }
