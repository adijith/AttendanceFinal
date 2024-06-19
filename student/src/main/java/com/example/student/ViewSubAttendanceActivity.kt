package com.example.student

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.student.databinding.ActivityViewSubAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewSubAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewSubAttendanceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var attendanceRef: DatabaseReference
    private lateinit var subjectId: String
    private lateinit var subjectName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSubAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get the subjectId and subjectName passed from the SubjectAdapter
        subjectId = intent.getStringExtra("subjectId") ?: ""
        subjectName = intent.getStringExtra("subjectName") ?: ""

        if (subjectId.isEmpty() || subjectName.isEmpty()) {
            // Handle case where subjectId or subjectName is not passed correctly
            finish() // Close the activity if subjectId or subjectName is invalid
            return
        }

        // Setup attendance reference
        val studentId = firebaseAuth.currentUser?.uid ?: ""
        attendanceRef = database.getReference("attendance").child("IT-2012").child(studentId).child(subjectId)

        // Set the subject name as the header of the activity
        binding.subjectNameTextView.text = subjectName

        // Apply background color
        binding.root.setBackgroundColor(Color.parseColor("#E0BBE4")) // Lilac color

        // Fetch and display attendance data
        fetchAndDisplayAttendance()
    }

    private fun fetchAndDisplayAttendance() {
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear previous views if any
                binding.attendanceTable.removeAllViews()

                // Create header row for the table
                val headerRow = TableRow(this@ViewSubAttendanceActivity)
                headerRow.addView(createTableCell("Date", true))
                headerRow.addView(createTableCell("Status", true))
                binding.attendanceTable.addView(headerRow)

                // Iterate through attendance entries and populate the table
                for (entrySnapshot in snapshot.children) {
                    val date = entrySnapshot.key ?: ""
                    val status = entrySnapshot.value?.toString() ?: "Absent"

                    val dataRow = TableRow(this@ViewSubAttendanceActivity)
                    dataRow.addView(createTableCell(date))
                    dataRow.addView(createTableCell(status))
                    binding.attendanceTable.addView(dataRow)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Helper function to create a TextView for table cell
    private fun createTableCell(text: String, isHeader: Boolean = false): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setPadding(16, 8, 16, 8)
        textView.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (isHeader) {
            textView.setTextColor(Color.WHITE)
            textView.setBackgroundColor(Color.parseColor("#6A5ACD")) // SlateBlue color
        }
        return textView
    }
}