package com.example.teacher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityAttenMarkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AttenMarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttenMarkBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var classSpinner: Spinner
    private var idCounter = 1

    // Function to generate a new unique ID
    private fun generateUniqueId(): Int {
        return idCounter++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttenMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classSpinner = findViewById(R.id.classSpinner)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentDate = getCurrentDate()
        binding.dateText.text = currentDate

        val teacherId = firebaseAuth.currentUser!!.uid
        fetchSubjectInfos(teacherId) { subjectInfos ->
            populateSpinner(subjectInfos)

            binding.showStudentsButton.setOnClickListener {
                val selectedSubject = classSpinner.selectedItem as Pair<String, String>
                val subjectName = selectedSubject.first // Extract subject name from Pair
                val subjectId = selectedSubject.second // Extract subject ID from Pair

                val yearNode = subjectId.split("-").take(2).joinToString("-")

                val database = FirebaseDatabase.getInstance()
                val attendanceRef = database.getReference("attendance").child(yearNode)

                // Fetch student UIDs under the yearNode in the Attendance node
                attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(attendanceSnapshot: DataSnapshot) {
                        val studentUids = mutableListOf<String>()
                        for (studentSnapshot in attendanceSnapshot.children) {
                            if (studentSnapshot.key != "teacherUid") { // Exclude the teacherId node
                                studentUids.add(studentSnapshot.key ?: "")
                            }
                        }

                        // Now fetch the student names from the Students node
                        fetchStudentNames(studentUids) { studentInfoList ->
                            // Clear previous rows (if any)
                            binding.studentsTableLayout.removeViews(
                                1,
                                binding.studentsTableLayout.childCount - 1
                            )

                            // Add a row for each student
                            for (studentInfo in studentInfoList) {
                                val tableRow = TableRow(this@AttenMarkActivity)
                                tableRow.layoutParams = TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                                )

                                // Create TextView for Role Number
                                val roleNumberTextView = TextView(this@AttenMarkActivity)
                                roleNumberTextView.text = studentInfo.roleNumber
                                roleNumberTextView.setPadding(8, 8, 8, 8)
                                roleNumberTextView.setTextColor(Color.WHITE)

                                // Create TextView for Student Name
                                val studentNameTextView = TextView(this@AttenMarkActivity)
                                studentNameTextView.text = studentInfo.name
                                studentNameTextView.setPadding(8, 8, 8, 8)
                                studentNameTextView.setTextColor(Color.WHITE)


                                // Create RadioGroup for attendance status
                                val radioGroup = RadioGroup(this@AttenMarkActivity)
                                radioGroup.orientation = RadioGroup.HORIZONTAL


                                // Create RadioButton for Present
                                val radioButtonPresent = RadioButton(this@AttenMarkActivity)
                                radioButtonPresent.id = generateUniqueId() // Generate a unique ID
                                radioButtonPresent.text = "Present"
                                radioButtonPresent.setTextColor(Color.WHITE)


                                // Create RadioButton for Absent
                                val radioButtonAbsent = RadioButton(this@AttenMarkActivity)
                                radioButtonAbsent.id = generateUniqueId() // Generate a unique ID
                                radioButtonAbsent.text = "Absent"
                                radioButtonAbsent.setTextColor(Color.WHITE)

                                // Set initial selection to "Absent"
                                radioButtonAbsent.isChecked = true

                                // Add RadioButtons to the RadioGroup
                                radioGroup.addView(radioButtonPresent)
                                radioGroup.addView(radioButtonAbsent)

                                // Add TextViews and RadioGroup to the TableRow
                                tableRow.addView(roleNumberTextView)
                                tableRow.addView(studentNameTextView)
                                tableRow.addView(radioGroup)

                                // Add the TableRow to the TableLayout
                                binding.studentsTableLayout.addView(tableRow)

                                // Store references to the student information and radio buttons in the TableRow tag
                                tableRow.tag = Pair(studentInfo, radioGroup)
                            }
                            binding.submitAttendanceButton.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error appropriately
                        Toast.makeText(this@AttenMarkActivity, "Failed to load attendance: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            binding.submitAttendanceButton.setOnClickListener {
                val currentDate = getCurrentDate()
                val selectedSubject = classSpinner.selectedItem as Pair<String, String>
                val subjectId = selectedSubject.second // Extract the subject ID
                val yearNode = subjectId.split("-").take(2).joinToString("-")

                val selectedPeriod = when (binding.PeriodRadioGroup.checkedRadioButtonId) {
                    R.id.radio1 -> "1"
                    R.id.radio2 -> "2"
                    R.id.radio3 -> "3"
                    R.id.radio4 -> "4"
                    R.id.radio5 -> "5"
                    R.id.radio6 -> "6"
                    else -> "1" // Default to period 1 if none is selected
                }

                val dateWithPeriod = "$currentDate-$selectedPeriod"

                val database = FirebaseDatabase.getInstance()

                // Check if the dateWithPeriod already exists in the schedule node
                val scheduleRef = database.getReference("Schedule").child(yearNode)
                scheduleRef.child(dateWithPeriod).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(scheduleSnapshot: DataSnapshot) {
                        if (scheduleSnapshot.exists()) {
                            // Period is already taken
                            Toast.makeText(this@AttenMarkActivity, "Period is already taken", Toast.LENGTH_SHORT).show()
                        } else {
                            // Check the number of entries under the yearNode in the schedule
                            scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.childrenCount >= 18) {
                                        // Find the earliest entry and remove it
                                        val earliestEntry = snapshot.children.minByOrNull { it.key.toString() }
                                        earliestEntry?.ref?.removeValue()
                                    }

                                    // Add the current dateWithPeriod to the schedule
                                    scheduleRef.child(dateWithPeriod).setValue(teacherId)
                                        .addOnSuccessListener {
                                            // After successfully adding the teacherId, add the subjectId and teacherId as child nodes
                                            val dateNodeRef = scheduleRef.child(dateWithPeriod)
                                            dateNodeRef.child("subjectId").setValue(subjectId)
                                            dateNodeRef.child("teacherId").setValue(teacherId)
                                                .addOnSuccessListener {
                                                    // Subject ID successfully added, proceed with updating attendance
                                                    for (i in 1 until binding.studentsTableLayout.childCount) {
                                                        val row = binding.studentsTableLayout.getChildAt(i) as TableRow
                                                        val (studentInfo, radioGroup) = row.tag as Pair<StudentInfo, RadioGroup>

                                                        val attendanceStatus = when (radioGroup.checkedRadioButtonId) {
                                                            radioGroup.getChildAt(0).id -> "Present"
                                                            radioGroup.getChildAt(1).id -> "Absent"
                                                            else -> "Absent"
                                                        }

                                                        // Update the attendance data in Firebase under the student's UID
                                                        val studentAttendanceRef = database.getReference("attendance").child(yearNode)
                                                            .child(studentInfo.uid).child(subjectId).child(dateWithPeriod)
                                                        studentAttendanceRef.setValue(attendanceStatus)
                                                            .addOnSuccessListener {
                                                                Log.d("AttenMarkActivity", "Attendance for ${studentInfo.name} updated successfully")
                                                            }
                                                            .addOnFailureListener { error ->
                                                                Log.e("AttenMarkActivity", "Failed to update attendance for ${studentInfo.name}: ${error.message}")
                                                            }
                                                    }

                                                    Toast.makeText(this@AttenMarkActivity, "Attendance saved successfully", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { error ->
                                                    Log.e("AttenMarkActivity", "Failed to add subjectId to schedule: ${error.message}")
                                                    Toast.makeText(this@AttenMarkActivity, "Failed to add subjectId to schedule: ${error.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { error ->
                                            Log.e("AttenMarkActivity", "Failed to update schedule: ${error.message}")
                                            Toast.makeText(this@AttenMarkActivity, "Failed to update schedule: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle the error appropriately
                                    Log.e("AttenMarkActivity", "Failed to read schedule data: ${error.message}")
                                    Toast.makeText(this@AttenMarkActivity, "Failed to read schedule data: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error appropriately
                        Log.e("AttenMarkActivity", "Failed to read schedule data: ${error.message}")
                        Toast.makeText(this@AttenMarkActivity, "Failed to read schedule data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }
    }
    private fun populateSpinner(subjectInfoList: List<Pair<String, String>>) {
        val adapter = ArrayAdapter<Pair<String, String>>(this@AttenMarkActivity,
            R.layout.spinner_item,
            subjectInfoList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        classSpinner.adapter = adapter
    }

}


private fun fetchStudentNames(studentUids: List<String>, callback: (List<StudentInfo>) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val studentsRef = database.getReference("Students")

    val studentInfoList = mutableListOf<StudentInfo>()
    var processedCount = 0

    for (uid in studentUids) {
        studentsRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                val isActive = studentSnapshot.child("active").getValue(Boolean::class.java) ?: false
                if (isActive) {
                    val studentName = studentSnapshot.child("name").value?.toString() ?: "Unknown Name"
                    val roleNumber = studentSnapshot.child("studentId").value?.toString() ?: "Unknown Role Number"
                    studentInfoList.add(StudentInfo(uid, roleNumber, studentName))
                }
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


private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}


private fun fetchSubjectInfos(teacherId: String, callback: (List<Pair<String, String>>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val subjectsRef = database.getReference("Teachers").child(teacherId).child("subjects")
        val subjectsNodeRef = database.getReference("Subjects")

        subjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subjectInfoList = mutableListOf<Pair<String, String>>()
                val tasks = mutableListOf<Pair<String, DatabaseReference>>()

                // Collect tasks to fetch subject names from the Subjects node
                for (subjectSnapshot in snapshot.children) {
                    val subjectId = subjectSnapshot.key ?: "" // Assuming subjectId is the key
                    val baseSubjectId = subjectId.split("-").take(2).joinToString("-") // Extract IT-2021 part

                    // Add the task to the list with the subjectId and its reference
                    tasks.add(Pair(subjectId, subjectsNodeRef.child(baseSubjectId).child(subjectId)))
                }

                // Process each task to get the subject name
                var processedCount = 0

                for ((subjectId, taskRef) in tasks) {
                    taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(subjectSnapshot: DataSnapshot) {
                            val subjectName = subjectSnapshot.child("subjectName").value?.toString() ?: "Unknown Subject"

                            // Add the result with the subject name and subjectId
                            subjectInfoList.add(Pair(subjectName, subjectId))
                            processedCount++

                            if (processedCount == tasks.size) {
                                // Once all tasks are processed, call the callback with the final list
                                callback(subjectInfoList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle the error appropriately
                            processedCount++
                            if (processedCount == tasks.size) {
                                callback(subjectInfoList) // Return what we have even if there's an error
                            }
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately
                callback(emptyList())
            }
        })
    }


