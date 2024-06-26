package com.example.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.databinding.ActivityViewTeacherBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewTeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTeacherBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch the teacher details
        fetchTeacherDetails()
    }

    private fun fetchTeacherDetails() {
        // Reference to the Teachers node
        val teachersRef = database.reference.child("Teachers")

        teachersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (teacherSnapshot in dataSnapshot.children) {
                    val teacherUid = teacherSnapshot.key
                    val teacherName = teacherSnapshot.child("fname").getValue(String::class.java) ?: "Unknown"
                    val teacherId = teacherSnapshot.child("teacherId").getValue(String::class.java) ?: "Unknown"

                    if (teacherUid != null) {
                        // Add teacher details to the table
                        addTeacherRow(teacherId, teacherName, teacherUid,)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViewTeacherActivity", "Error fetching teacher details: ${databaseError.message}")
                Toast.makeText(this@ViewTeacherActivity, "Error fetching teacher details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTeacherRow(teacherId:String, name: String, teacherUid: String) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // Teacher Name
        val textViewName = TextView(this)
        textViewName.text = name
        textViewName.setPadding(16, 16, 16, 16)
        tableRow.addView(textViewName)

        // Teacher ID
        val textViewTeacherId = TextView(this)
        textViewTeacherId.text = teacherId
        textViewTeacherId.setPadding(16, 16, 16, 16)
        tableRow.addView(textViewTeacherId)

        // View Profile Button
        val buttonViewProfile = Button(this)
        buttonViewProfile.text = "View Profile"
        buttonViewProfile.setPadding(16, 16, 16, 16)
        buttonViewProfile.setOnClickListener {
            // Handle view profile click
            val intent = Intent(this, ViewTeacherProfileActivity::class.java)
            intent.putExtra("TEACHER_ID", teacherUid)
            startActivity(intent)
        }
        tableRow.addView(buttonViewProfile)

        // Add the row to the table
        binding.tableLayoutTeachers.addView(tableRow)
    }
}
