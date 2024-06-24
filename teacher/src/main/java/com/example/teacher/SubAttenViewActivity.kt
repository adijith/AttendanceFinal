package com.example.teacher

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivitySubAttenViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SubAttenViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubAttenViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var subjectSpinner: Spinner
    private lateinit var studentSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var viewAttendanceButton: Button
    private lateinit var attendanceTable: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubAttenViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and database reference
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize UI elements
        subjectSpinner = binding.subjectSpinner
        studentSpinner = binding.studentSpinner
        submitButton = binding.submitButton
        viewAttendanceButton = binding.viewAttendanceButton
        attendanceTable = binding.attendanceTable

        // Get current teacher's ID
        val teacherId = firebaseAuth.currentUser!!.uid

        // Fetch subject IDs and display in subject spinner
        fetchSubjectIds(teacherId)

        // Set onClickListener for the submit button
        submitButton.setOnClickListener {
            val selectedSubject = subjectSpinner.selectedItem.toString()
            val subjectId = extractSubjectId(selectedSubject)

            fetchStudentDetails(subjectId)
        }

        // Set onClickListener for the view attendance button
        viewAttendanceButton.setOnClickListener {
            val selectedStudent = studentSpinner.selectedItem.toString()
            val studentId = extractStudentId(selectedStudent)
            val selectedSubject = subjectSpinner.selectedItem.toString()
            val subjectId = extractSubjectId(selectedSubject)

            displayAttendance(studentId, subjectId)
        }
    }

    private fun fetchSubjectIds(teacherId: String) {
        val teacherSubjectsRef = databaseReference.child("Teachers").child(teacherId).child("subjects")

        teacherSubjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val subjectIds = mutableListOf<String>()
                    for (subjectSnapshot in snapshot.children) {
                        val subjectId = subjectSnapshot.key
                        subjectIds.add(subjectId!!)
                    }
                    fetchSubjectNames(subjectIds)
                } else {
                    Toast.makeText(this@SubAttenViewActivity, "No subjects found for this teacher.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SubAttenViewActivity, "Failed to fetch subjects: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSubjectNames(subjectIds: List<String>) {
        val subjectNames = mutableListOf<String>()

        for (subjectId in subjectIds) {
            val classId = subjectId.substringBeforeLast("-")
            val classSubjectRef = databaseReference.child("Subjects").child(classId).child(subjectId)

            classSubjectRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subjectName = snapshot.child("subjectName").getValue(String::class.java)
                        if (subjectName != null) {
                            subjectNames.add("$subjectName ($subjectId)")

                            if (subjectNames.size == subjectIds.size) {
                                displaySubjectsInSpinner(subjectNames)
                            }
                        }
                    } else {
                        Toast.makeText(this@SubAttenViewActivity, "Subject not found: $subjectId", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SubAttenViewActivity, "Failed to fetch subject name: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun displaySubjectsInSpinner(subjectNames: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, subjectNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter
    }

    private fun extractSubjectId(selectedSubject: String): String {
        return selectedSubject.substringAfterLast("(").substringBeforeLast(")")
    }

    private fun fetchStudentDetails(subjectId: String) {
        val classId = subjectId.substringBeforeLast("-")
        val attendanceRef = databaseReference.child("attendance").child(classId)

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val studentIds = mutableListOf<String>()
                    for (studentSnapshot in snapshot.children) {
                        val studentId = studentSnapshot.key
                        if (studentId != null && studentId != "teacherUid") {
                            studentIds.add(studentId)
                        }
                    }
                    fetchStudentNames(studentIds)
                } else {
                    Toast.makeText(this@SubAttenViewActivity, "No students found for this class.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SubAttenViewActivity, "Failed to fetch student details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStudentNames(studentIds: List<String>) {
        val studentDetails = mutableListOf<String>()

        for (studentId in studentIds) {
            val studentRef = databaseReference.child("Students").child(studentId)

            studentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val studentName = snapshot.child("name").getValue(String::class.java)
                        if (studentName != null) {
                            studentDetails.add("$studentName ($studentId)")

                            if (studentDetails.size == studentIds.size) {
                                displayStudentsInSpinner(studentDetails)
                            }
                        }
                    } else {
                        Toast.makeText(this@SubAttenViewActivity, "Student not found: $studentId", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SubAttenViewActivity, "Failed to fetch student name: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun displayStudentsInSpinner(studentDetails: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, studentDetails)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        studentSpinner.adapter = adapter

        studentSpinner.visibility = View.VISIBLE
        viewAttendanceButton.visibility = View.VISIBLE
    }

    private fun extractStudentId(selectedStudent: String): String {
        return selectedStudent.substringAfterLast("(").substringBeforeLast(")")
    }

    private fun displayAttendance(studentId: String, subjectId: String) {
        val classId = subjectId.substringBeforeLast("-")
        val studentAttendanceRef = databaseReference.child("attendance").child(classId).child(studentId).child(subjectId)

        studentAttendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val attendanceRecords = mutableListOf<AttendanceRecord>()
                    for (dateSnapshot in snapshot.children) {
                        val date = dateSnapshot.key
                        val status = dateSnapshot.getValue(String::class.java)
                        if (date != null && status != null) {
                            attendanceRecords.add(AttendanceRecord(date, status))
                        }
                    }
                    displayAttendanceTable(attendanceRecords)
                } else {
                    Toast.makeText(this@SubAttenViewActivity, "No attendance records found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SubAttenViewActivity, "Failed to fetch attendance records: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayAttendanceTable(attendanceRecords: List<AttendanceRecord>) {
        // Clear any existing rows in the table
        attendanceTable.removeAllViews()

        // Create table headers
        val headerRow = TableRow(this)

        val dateHeader = TextView(this).apply {
            text = "Date"
            setPadding(16, 16, 16, 16)
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.white))
            setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
        headerRow.addView(dateHeader)

        val statusHeader = TextView(this).apply {
            text = "Status"
            setPadding(16, 16, 16, 16)
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.white))
            setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
        headerRow.addView(statusHeader)

        attendanceTable.addView(headerRow)

        // Populate table with attendance records
        var presentCount = 0
        for (record in attendanceRecords) {
            val row = TableRow(this)

            val dateText = TextView(this).apply {
                text = record.date
                setPadding(16, 16, 16, 16)
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.white))
            }
            row.addView(dateText)

            val statusText = TextView(this).apply {
                text = record.status
                setPadding(16, 16, 16, 16)
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.white))
            }
            row.addView(statusText)

            attendanceTable.addView(row)

            if (record.status == "Present") {
                presentCount++
            }
        }

        // Calculate and display attendance percentage
        val totalClasses = attendanceRecords.size
        val attendancePercentage = if (totalClasses > 0) (presentCount * 100) / totalClasses else 0
        val studentName = studentSpinner.selectedItem.toString().substringBeforeLast(" (")
        val headerText = "$studentName - Attendance: $attendancePercentage%"
        Toast.makeText(this, headerText, Toast.LENGTH_LONG).show()
    }

    data class AttendanceRecord(val date: String, val status: String)
}
