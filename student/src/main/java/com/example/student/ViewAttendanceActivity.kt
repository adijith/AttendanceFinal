package com.example.student

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.student.databinding.ActivityViewAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAttendanceBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Get the current student ID
        val studentId = firebaseAuth.currentUser?.uid
        if (studentId != null) {
            // Fetch and display attendance data
            Log.d("ViewAttendance", "Fetching attendance for student ID: $studentId")
            fetchAndDisplayAttendance(studentId)
        } else {
            Log.e("ViewAttendance", "Failed to get current user ID")
        }
    }

    private fun fetchAndDisplayAttendance(studentId: String) {
        val database = FirebaseDatabase.getInstance()
        val studentsRef = database.getReference("Students").child(studentId)

        // Fetch the student's class (sclass)
        studentsRef.child("sclass").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sclass = dataSnapshot.value?.toString() ?: "Unknown Class"
                Log.d("ViewAttendance", "Student class: $sclass")

                // Fetch attendance data using the sclass
                val attendanceRef = database.getReference("attendance").child(sclass).child(studentId)
                attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(attendanceSnapshot: DataSnapshot) {
                        val subjectAttendanceMap = mutableMapOf<String, Float>()
                        Log.d("ViewAttendance", "Attendance data fetched for student ID: $studentId")

                        var totalClasses = 0
                        var presentCount = 0

                        for (subjectSnapshot in attendanceSnapshot.children) {
                            val subjectId = subjectSnapshot.key ?: continue
                            val attendanceEntries = subjectSnapshot.children.toList() // Convert to list for easier counting

                            // Calculate total and present classes for the subject
                            val totalSubjectClasses = attendanceEntries.size
                            val presentSubjectClasses = attendanceEntries.count { it.value == "Present" }

                            Log.d("ViewAttendance", "Processing subject ID: $subjectId")

                            // Calculate the attendance percentage for the subject
                            val attendancePercentage = if (totalSubjectClasses > 0) {
                                (presentSubjectClasses.toFloat() / totalSubjectClasses.toFloat()) * 100
                            } else {
                                0f
                            }

                            subjectAttendanceMap[subjectId] = attendancePercentage
                            Log.d("ViewAttendance", "Subject ID: $subjectId, Total Classes: $totalSubjectClasses, Present Count: $presentSubjectClasses, Attendance: $attendancePercentage%")

                            // Accumulate total classes and present count for overall calculation
                            totalClasses += totalSubjectClasses
                            presentCount += presentSubjectClasses
                        }

                        // Calculate overall attendance percentage
                        val overallAttendancePercentage = if (totalClasses > 0) {
                            (presentCount.toFloat() / totalClasses.toFloat()) * 100
                        } else {
                            0f
                        }
                        Log.d("ViewAttendance", "Overall Attendance Percentage: $overallAttendancePercentage%")

                        // Optionally, fetch subject names using the subject IDs and sclass
                        fetchSubjectNames(sclass, subjectAttendanceMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ViewAttendance", "Error fetching attendance data", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewAttendance", "Error fetching student class", error.toException())
            }
        })
    }

    private fun fetchSubjectNames(sclass: String, subjectAttendanceMap: Map<String, Float>) {
        val database = FirebaseDatabase.getInstance()
        val subjectsRef = database.getReference("Subjects").child(sclass)

        val subjectInfoList = mutableListOf<Triple<String, String, Float>>()
        var processedCount = 0

        for ((subjectId, attendancePercentage) in subjectAttendanceMap) {
            subjectsRef.child(subjectId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(subjectSnapshot: DataSnapshot) {
                    val subjectName = subjectSnapshot.child("subjectName").value?.toString() ?: "Unknown Subject"
                    subjectInfoList.add(Triple(subjectId, subjectName, attendancePercentage))
                    processedCount++

                    Log.d("ViewAttendance", "Subject fetched: $subjectName (ID: $subjectId) with attendance $attendancePercentage%")

                    if (processedCount == subjectAttendanceMap.size) {
                        // Once all subject names are fetched, display them in the layout
                        Log.d("ViewAttendance", "All subjects processed: $subjectInfoList")
                        displaySubjectBoxes(subjectInfoList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewAttendance", "Error fetching subject name for ID: $subjectId", error.toException())
                    processedCount++
                    if (processedCount == subjectAttendanceMap.size) {
                        // Even if there's an error, display what we have
                        displaySubjectBoxes(subjectInfoList)
                    }
                }
            })
        }
    }

    // Method to display subject boxes in a RecyclerView
    private fun displaySubjectBoxes(subjectInfoList: List<Triple<String, String, Float>>) {
        Log.d("ViewAttendance", "Displaying subjects in RecyclerView")
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SubjectAdapter(subjectInfoList)
    }
}