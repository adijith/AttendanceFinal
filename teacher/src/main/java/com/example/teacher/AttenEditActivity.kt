package com.example.teacher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityAttenEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AttenEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttenEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttenEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Get the current teacher's ID
        val teacherId = firebaseAuth.currentUser?.uid

        // Initialize selectedDate with the current date
        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Initialize date text view
        binding.dateText.text = selectedDate

        if (teacherId != null) {
            val database = FirebaseDatabase.getInstance()
            val teachersRef = database.getReference("Teachers").child(teacherId)

            teachersRef.child("tclass").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tclass = dataSnapshot.getValue(String::class.java)

                    if (tclass != null) {
                        Log.d("AttenEditActivity", "Teacher's class: $tclass")
                        // Assuming you have a TextView for class year node
                        binding.dateText.text = "Year: $tclass"

                        // Fetch subjects
                        fetchSubjects(tclass) { subjectMap ->
                            Log.d("AttenEditActivity", "Fetched subjects: $subjectMap")

                            // Fetch student info
                            fetchStudentInfo(tclass) { studentInfoList ->
                                Log.d("AttenEditActivity", "Fetched student info: $studentInfoList")

                                // Process and display attendance statistics for each student
                                processAndDisplayAttendance(tclass, studentInfoList, subjectMap)
                            }
                        }
                    } else {
                        Log.e("FetchTClass", "Teacher does not have a class assigned")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchTClass", "Failed to fetch teacher class: ${error.message}")
                }
            })
        } else {
            Log.e("FetchTClass", "Teacher ID is null")
        }

        binding.showStudentsButton.setOnClickListener {
            fetchAttendanceForDate(selectedDate)
        }

        binding.submitAttendanceButton.setOnClickListener {
            saveAttendanceChanges()
        }
    }

    private fun fetchSubjects(tclass: String, callback: (Map<String, String>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val subjectsRef = database.getReference("Subjects").child(tclass)

        subjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subjectMap = mutableMapOf<String, String>()

                for (subjectSnapshot in dataSnapshot.children) {
                    val subjectId = subjectSnapshot.key ?: ""
                    val subjectName = subjectSnapshot.child("subjectName").getValue(String::class.java) ?: ""

                    if (subjectId.isNotEmpty() && subjectName.isNotEmpty()) {
                        subjectMap[subjectId] = subjectName
                    }
                }

                callback(subjectMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchSubjects", "Failed to fetch subjects: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    private fun fetchStudentInfo(tclass: String, callback: (List<StudentInfo>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val studentsRef = database.getReference("Students").orderByChild("sclass").equalTo(tclass)

        studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentInfoList = mutableListOf<StudentInfo>()

                for (studentSnapshot in dataSnapshot.children) {
                    val student = studentSnapshot.getValue(StudentInfo::class.java)
                    if (student != null) {
                        studentInfoList.add(student)
                    }
                }

                callback(studentInfoList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchStudents", "Failed to fetch students: ${error.message}")
                callback(emptyList())
            }
        })
    }

    private fun processAndDisplayAttendance(
        tclass: String,
        studentInfoList: List<StudentInfo>,
        subjectMap: Map<String, String>
    ) {
        val database = FirebaseDatabase.getInstance()
        val attendanceRef = database.getReference("Attendance").child(tclass).child(selectedDate)

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentAttendanceMap = mutableMapOf<String, Boolean>()

                for (studentSnapshot in dataSnapshot.children) {
                    val studentId = studentSnapshot.key ?: ""
                    val isPresent = studentSnapshot.getValue(Boolean::class.java) ?: false

                    if (studentId.isNotEmpty()) {
                        studentAttendanceMap[studentId] = isPresent
                    }
                }

                displayStudentAttendance(studentInfoList, studentAttendanceMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProcessAttendance", "Failed to fetch attendance data: ${error.message}")
            }
        })
    }

    private fun displayStudentAttendance(
        studentInfoList: List<StudentInfo>,
        studentAttendanceMap: Map<String, Boolean>
    ) {
        binding.studentsTableLayout.removeAllViews()

        // Create header row
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(Color.DKGRAY)

        val headerRollNumber = TextView(this)
        headerRollNumber.text = "Roll Number"
        headerRollNumber.setPadding(8, 8, 8, 8)
        headerRollNumber.setTextColor(Color.WHITE)
        headerRow.addView(headerRollNumber)

        val headerStudentName = TextView(this)
        headerStudentName.text = "Student Name"
        headerStudentName.setPadding(8, 8, 8, 8)
        headerStudentName.setTextColor(Color.WHITE)
        headerRow.addView(headerStudentName)

        val headerOverallAttendance = TextView(this)
        headerOverallAttendance.text = "Overall Attendance"
        headerOverallAttendance.setPadding(8, 8, 8, 8)
        headerOverallAttendance.setTextColor(Color.WHITE)
        headerRow.addView(headerOverallAttendance)

        binding.studentsTableLayout.addView(headerRow)

        // Add rows for each student
        for (student in studentInfoList) {
            val row = TableRow(this)
            row.setBackgroundColor(Color.LTGRAY)

            val rollNumber = TextView(this)
            rollNumber.text = student.studentId
            rollNumber.setPadding(8, 8, 8, 8)
            row.addView(rollNumber)

            val studentName = TextView(this)
            studentName.text = student.studentName
            studentName.setPadding(8, 8, 8, 8)
            row.addView(studentName)

            val overallAttendance = TextView(this)
            val isPresent = studentAttendanceMap[student.studentId] ?: false
            overallAttendance.text = if (isPresent) "Present" else "Absent"
            overallAttendance.setPadding(8, 8, 8, 8)
            overallAttendance.setTextColor(if (isPresent) Color.GREEN else Color.RED)
            row.addView(overallAttendance)

            binding.studentsTableLayout.addView(row)
        }

        binding.submitAttendanceButton.visibility = View.VISIBLE
    }

    private fun fetchAttendanceForDate(date: String) {
        // Implement this function to fetch and display attendance data for the selected date
        Toast.makeText(this, "Fetching attendance for date: $date", Toast.LENGTH_SHORT).show()
    }

    private fun saveAttendanceChanges() {
        // Implement this function to save the changes made to attendance
        Toast.makeText(this, "Saving attendance changes", Toast.LENGTH_SHORT).show()
    }

    data class StudentInfo(val studentId: String, val studentName: String)
}
