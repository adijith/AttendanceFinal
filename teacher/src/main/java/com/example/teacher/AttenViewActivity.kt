package com.example.teacher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityAttenViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


data class StudentAttendance(
    val studentInfo: StudentInfo,
    val attendancePercentage: Double,
    val subjectAttendance: Map<String, Double> // Maps subject name to attendance percentage
)

class AttenViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttenViewBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttenViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Get the current teacher's ID
        val teacherId = firebaseAuth.currentUser?.uid

        if (teacherId != null) {
            val database = FirebaseDatabase.getInstance()
            val teachersRef = database.getReference("Teachers").child(teacherId)

            teachersRef.child("tclass").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tclass = dataSnapshot.getValue(String::class.java)

                    if (tclass != null) {
                        Log.d("AttenViewActivity", "Teacher's class: $tclass")
                        binding.classYearNode.text = "Year: $tclass"

                        // Fetch subjects
                        fetchSubjects(tclass) { subjectMap ->
                            Log.d("AttenViewActivity", "Fetched subjects: $subjectMap")

                            // Fetch student info
                            fetchStudentInfo(tclass) { studentInfoList ->
                                Log.d("AttenViewActivity", "Fetched student info: $studentInfoList")

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
        val studentsRef = database.getReference("attendance").child(tclass)

        studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentUids = mutableListOf<String>()

                for (studentSnapshot in dataSnapshot.children) {
                    val studentId = studentSnapshot.key ?: ""
                    if (studentId.isNotEmpty() && studentId != "teacherUid") {
                        studentUids.add(studentId)
                    }
                }

                // Fetch detailed student info using the list of UIDs
                if (studentUids.isNotEmpty()) {
                    fetchStudentNames(studentUids, callback)
                } else {
                    Log.e("FetchStudentInfo", "No student UIDs found for class $tclass")
                    callback(emptyList()) // No students to process
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchStudentInfo", "Failed to fetch student UIDs: ${error.message}")
                callback(emptyList())
            }
        })
    }

    private fun fetchStudentNames(studentUids: List<String>, callback: (List<StudentInfo>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val studentsRef = database.getReference("Students")

        val studentInfoList = mutableListOf<StudentInfo>()
        var processedCount = 0

        for (uid in studentUids) {
            studentsRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(studentSnapshot: DataSnapshot) {
                        val studentName = studentSnapshot.child("name").getValue(String::class.java)
                            ?: "Unknown Name"
                        val roleNumber = studentSnapshot.child("studentId").getValue(String::class.java)
                                ?: "Unknown Role Number"
                        studentInfoList.add(StudentInfo(uid, roleNumber, studentName))
                        processedCount++

                        if (processedCount == studentUids.size) {
                            // Once all names are fetched, call the callback with the list of student IDs and names
                            callback(studentInfoList)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    processedCount++
                    if (processedCount == studentUids.size) {
                        callback(studentInfoList) // Return what we have even if there's an error
                    }
                }
            })
        }
    }

    private fun processAndDisplayAttendance(
        tclass: String,
        studentInfoList: List<StudentInfo>,
        subjectMap: Map<String, String>
    ) {
        val database = FirebaseDatabase.getInstance()
        val attendanceRef = database.getReference("attendance").child(tclass)

        // Process attendance for each student
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentAttendanceList = mutableListOf<StudentAttendance>()

                for (studentInfo in studentInfoList) {
                    val studentSnapshot = dataSnapshot.child(studentInfo.uid)
                    val subjectAttendance = mutableMapOf<String, Double>()
                    var totalClasses = 0
                    var presentCount = 0

                    for ((subjectId, subjectName) in subjectMap) {
                        val subjectSnapshot = studentSnapshot.child(subjectId)
                        val totalSubjectClasses = subjectSnapshot.childrenCount.toInt()
                        val presentSubjectClasses =
                            subjectSnapshot.children.count { it.value == "Present" }

                        subjectAttendance[subjectName] = if (totalSubjectClasses > 0) {
                            (presentSubjectClasses.toDouble() / totalSubjectClasses.toDouble()) * 100
                        } else {
                            0.0
                        }

                        totalClasses += totalSubjectClasses
                        presentCount += presentSubjectClasses
                    }

                    val attendancePercentage = if (totalClasses > 0) {
                        (presentCount.toDouble() / totalClasses.toDouble()) * 100
                    } else {
                        0.0
                    }

                    studentAttendanceList.add(
                        StudentAttendance(
                            studentInfo,
                            attendancePercentage,
                            subjectAttendance
                        )
                    )
                }

                // Display all processed attendance data
                Log.d("ProcessAttendance", "Processed student attendance: $studentAttendanceList")
                displayAllAttendanceStats(studentAttendanceList, subjectMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProcessAttendance", "Failed to fetch attendance: ${error.message}")
            }
        })
    }

    private fun displayAllAttendanceStats(
        studentAttendanceList: List<StudentAttendance>,
        subjectMap: Map<String, String>
    ) {
        val tableLayout = binding.tableLayout

        // Clear existing rows except headers
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        // Display header row dynamically with subjects
        val headerRow = TableRow(this)

        // Roll Number TextView
        val rollNumberTextView = TextView(this).apply {
            text = "Roll Number"
            setTextStyle(this)
            setWeight(this, 1f)
            setTextColor(Color.BLACK)
        }
        headerRow.addView(rollNumberTextView)

        // Student Name TextView
        val nameTextView = TextView(this).apply {
            text = "Student Name"
            setTextStyle(this)
            setWeight(this, 2f)
            setTextColor(Color.BLACK)
        }
        headerRow.addView(nameTextView)

        // Overall Attendance Percentage TextView
        val overallAttendanceTextView = TextView(this).apply {
            text = "Overall Attendance %"
            setTextStyle(this)
            setWeight(this, 1f)
            setTextColor(Color.BLACK)
        }
        headerRow.addView(overallAttendanceTextView)

        for ((_, subjectName) in subjectMap) {
            val subjectTextView = TextView(this).apply {
                text = subjectName
                setTextStyle(this)
                setWeight(this, 1f)
                setTextColor(Color.BLACK)
            }
            headerRow.addView(subjectTextView)
        }

        // Add the header row to the table layout
        tableLayout.addView(headerRow)

        // Display each student's attendance data
        for (studentAttendance in studentAttendanceList) {
            displayAttendanceStats(studentAttendance, subjectMap)
        }
    }

    private fun displayAttendanceStats(
        studentAttendance: StudentAttendance,
        subjectMap: Map<String, String>
    ) {
        val tableLayout = binding.tableLayout

        // Create a new row
        val row = TableRow(this)

        // Roll Number TextView
        val rollNumberTextView = TextView(this).apply {
            text = studentAttendance.studentInfo.roleNumber
            setTextStyle(this)
            setWeight(this, 1f)
            setTextColor(Color.BLACK)

        }
        row.addView(rollNumberTextView)

        // Student Name TextView
        val nameTextView = TextView(this).apply {
            text = studentAttendance.studentInfo.name
            setTextStyle(this)
            setWeight(this, 2f)
            setTextColor(Color.BLACK)

        }
        row.addView(nameTextView)

        // Overall Attendance Percentage TextView
        val overallAttendanceTextView = TextView(this).apply {
            text = "%.2f%%".format(studentAttendance.attendancePercentage)
            setTextStyle(this)
            setWeight(this, 1f)
            setTextColor(Color.BLACK)

        }
        row.addView(overallAttendanceTextView)

        // Subject-wise Attendance Percentages
        for ((subjectId, subjectName) in subjectMap) {
            val subjectAttendance = studentAttendance.subjectAttendance[subjectName] ?: 0.0

            val subjectAttendanceTextView = TextView(this).apply {
                text = "%.2f%%".format(subjectAttendance)
                setTextStyle(this)
                setWeight(this, 1f)
                setTextColor(Color.BLACK)
            }
            row.addView(subjectAttendanceTextView)
        }

        tableLayout.addView(row)
    }

    private fun setTextStyle(textView: TextView) {
        textView.textSize = 16f
        textView.setPadding(8, 8, 8, 8)
    }


    private fun setWeight(textView: TextView, weight: Float) {
        textView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
    }
}
