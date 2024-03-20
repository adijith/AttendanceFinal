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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                            Toast.makeText(this, "login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "login failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } else {
                Toast.makeText(this@LoginActivity, "all fields mandatory", Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }
}
