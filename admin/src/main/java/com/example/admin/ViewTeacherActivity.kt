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
                    val isActive = teacherSnapshot.child("active").getValue(Boolean::class.java) ?: false
                    if (isActive) {
                        val teacherUid = teacherSnapshot.key
                        val teacherName =
                            teacherSnapshot.child("fname").getValue(String::class.java) ?: "Unknown"
                        val teacherId =
                            teacherSnapshot.child("teacherId").getValue(String::class.java)
                                ?: "Unknown"

                        if (teacherUid != null) {
                            // Add teacher details to the table
                            addTeacherRow(teacherId, teacherName, teacherUid,)
                        }
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

        val buttonDeleteProfile = Button(this)
        buttonDeleteProfile.text = "Deactivate Teacher"
        buttonDeleteProfile.setPadding(16, 16, 16, 16)
        buttonDeleteProfile.setOnClickListener {
            // Firebase reference to the teacher's node
            val database = FirebaseDatabase.getInstance()
            val teacherRef = database.getReference("Teachers").child(teacherUid)

            // Set the `active` status to false
            teacherRef.child("active").setValue(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successful update
                    Log.d("DeactivateTeacher", "Teacher deactivated successfully.")

                    // Navigate to ViewTeacherActivity
                    val intent = Intent(this, ViewTeacherActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Close the current activity
                } else {
                    // Failed to update
                    Log.e("DeactivateTeacher", "Failed to deactivate teacher: ${task.exception?.message}")
                    // Optionally, you can show a Toast or an alert dialog here to inform the user.
                    Toast.makeText(this, "Failed to deactivate teacher", Toast.LENGTH_SHORT).show()
                }
            }
        }
        tableRow.addView(buttonDeleteProfile)

        // Add the row to the table
        binding.tableLayoutTeachers.addView(tableRow)
    }
}
