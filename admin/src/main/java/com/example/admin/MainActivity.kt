package com.example.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admin.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseReference: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//
        setContentView(binding.root)

        binding.logout.setOnClickListener {
            val intent = Intent(this@MainActivity, AddTeacherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}