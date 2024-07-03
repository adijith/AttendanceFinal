package com.example.teacher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityEditAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAttendanceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var idCounter = 1

    private fun generateUniqueId(): Int {
        return idCounter++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val teacherId = firebaseAuth.currentUser!!.uid

        fetchClasses { classList ->
            populateSpinner(binding.classSpinner, classList)
            binding.classSpinner.visibility = View.VISIBLE
        }

        binding.submitButton.setOnClickListener {
            val selectedClass = binding.classSpinner.selectedItem as String
            fetchDateAndSubjects(teacherId, selectedClass) { dateSubjectList ->
                populateSpinnerWithPairs(binding.dateSubjectSpinner, dateSubjectList)
                binding.dateSubjectSpinner.visibility = View.VISIBLE
                binding.showAttendanceButton.isEnabled = true
            }
        }

        binding.showAttendanceButton.setOnClickListener {
            val selectedClass = binding.classSpinner.selectedItem as String
            val selectedDateSubject = binding.dateSubjectSpinner.selectedItem as? Pair<String, String>
            if (selectedDateSubject != null) {
                val (displayText, dateAndSubjectId) = selectedDateSubject
                val (selectedDate, subjectId) = dateAndSubjectId.split(",")
                fetchAndDisplayAttendance(selectedClass, selectedDate, subjectId)
            } else {
                Toast.makeText(this, "Please select a date and subject", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveAttendanceButton.setOnClickListener {
            saveAttendanceChanges()
        }
    }

    private fun fetchClasses(callback: (List<String>) -> Unit) {
        val scheduleRef = database.getReference("Schedule")
        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classList = mutableListOf<String>()
                for (classSnapshot in snapshot.children) {
                    classList.add(classSnapshot.key.toString())
                }
                callback(classList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditAttendanceActivity, "Failed to fetch classes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchDateAndSubjects(teacherId: String, selectedClass: String, callback: (List<Pair<String, String>>) -> Unit) {
        val scheduleRef = database.getReference("Schedule").child(selectedClass)
        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dateSubjectList = mutableListOf<Pair<String, String>>()
                var fetchCount = 0
                val totalCount = snapshot.childrenCount

                for (dateSnapshot in snapshot.children) {
                    val teacherUid = dateSnapshot.child("teacherId").value?.toString()
                    if (teacherUid == teacherId) {
                        val subjectId = dateSnapshot.child("subjectId").value?.toString() ?: "Unknown"
                        val date = dateSnapshot.key.toString()
                        fetchSubjectName(subjectId) { subjectName ->
                            val item = Pair("$date - $subjectName", "$date,$subjectId")
                            dateSubjectList.add(item)
                            fetchCount++
                            if (fetchCount == totalCount.toInt()) {
                                callback(dateSubjectList)
                            }
                        }
                    } else {
                        fetchCount++
                    }
                }

                if (totalCount == 0L) {
                    callback(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditAttendanceActivity, "Failed to fetch dates and subjects: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Fetch dates and subjects cancelled: ${error.message}")
            }
        })
    }

    private fun fetchSubjectName(subjectId: String, callback: (String) -> Unit) {
        val baseSubjectId = subjectId.split("-").take(2).joinToString("-")
        val subjectRef = database.getReference("Subjects").child(baseSubjectId).child(subjectId)
        subjectRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subjectName = snapshot.child("subjectName").value?.toString() ?: "Unknown Subject"
                callback(subjectName)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Fetch subject name cancelled: ${error.message}")
                callback("Unknown Subject")
            }
        })
    }

    private fun fetchAndDisplayAttendance(selectedClass: String, selectedDate: String, subjectId: String) {
        val attendanceRef = database.getReference("attendance").child(selectedClass)

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(attendanceSnapshot: DataSnapshot) {
                val studentUids = mutableListOf<String>()
                for (studentSnapshot in attendanceSnapshot.children) {
                    if (studentSnapshot.key != "teacherUid") {
                        studentUids.add(studentSnapshot.key ?: "")
                    }
                }

                fetchStudentNamesAndAttendance(studentUids, selectedClass, selectedDate, subjectId) { studentInfoList ->
                    displayAttendance(studentInfoList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditAttendanceActivity, "Failed to load attendance: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStudentNamesAndAttendance(studentUids: List<String>, selectedClass: String, selectedDate: String, subjectId: String, callback: (List<StudentInfo>) -> Unit) {
        val studentsRef = database.getReference("Students")
        val attendanceRef = database.getReference("attendance").child(selectedClass)

        val studentInfoList = mutableListOf<StudentInfo>()
        var processedCount = 0

        for (uid in studentUids) {
            studentsRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(studentSnapshot: DataSnapshot) {
                    val studentName = studentSnapshot.child("name").value?.toString() ?: "Unknown"
                    val roleNumber = studentSnapshot.child("studentId").value?.toString() ?: "Unknown"

                    attendanceRef.child(uid).child(subjectId).child(selectedDate).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(attendanceSnapshot: DataSnapshot) {
                            val attendanceStatus = attendanceSnapshot.value?.toString() ?: "Absent"
                            studentInfoList.add(StudentInfo(uid, roleNumber, studentName, attendanceStatus))
                            processedCount++
                            if (processedCount == studentUids.size) {
                                callback(studentInfoList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Failed to fetch attendance: ${error.message}")
                            processedCount++
                            if (processedCount == studentUids.size) {
                                callback(studentInfoList)
                            }
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to fetch student info: ${error.message}")
                    processedCount++
                    if (processedCount == studentUids.size) {
                        callback(studentInfoList)
                    }
                }
            })
        }
    }

    private fun displayAttendance(studentInfoList: List<StudentInfo>) {
        binding.studentsTableLayout.removeAllViews()

        // Add header row
        val headerRow = TableRow(this)
        headerRow.addView(createTextView("Roll No", true))
        headerRow.addView(createTextView("Name", true))
        headerRow.addView(createTextView("Status", true))
        binding.studentsTableLayout.addView(headerRow)

        for (studentInfo in studentInfoList) {
            val tableRow = TableRow(this)
            tableRow.addView(createTextView(studentInfo.roleNumber))
            tableRow.addView(createTextView(studentInfo.name))

            val radioGroup = RadioGroup(this)
            radioGroup.orientation = RadioGroup.HORIZONTAL

            val radioButtonPresent = RadioButton(this)
            radioButtonPresent.id = generateUniqueId()
            radioButtonPresent.text = "Present"

            val radioButtonAbsent = RadioButton(this)
            radioButtonAbsent.id = generateUniqueId()
            radioButtonAbsent.text = "Absent"

            if (studentInfo.attendanceStatus == "Present") {
                radioButtonPresent.isChecked = true
            } else {
                radioButtonAbsent.isChecked = true
            }

            radioGroup.addView(radioButtonPresent)
            radioGroup.addView(radioButtonAbsent)
            tableRow.addView(radioGroup)

            tableRow.tag = Pair(studentInfo, radioGroup)
            binding.studentsTableLayout.addView(tableRow)
        }

        binding.saveAttendanceButton.visibility = View.VISIBLE
    }

    private fun createTextView(text: String, isHeader: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
            setTextColor(Color.BLACK)
            if (isHeader) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun saveAttendanceChanges() {
        val selectedClass = binding.classSpinner.selectedItem as String
        val selectedDateSubject = binding.dateSubjectSpinner.selectedItem as Pair<String, String>
        val (_, dateAndSubjectId) = selectedDateSubject
        val (selectedDate, subjectId) = dateAndSubjectId.split(",")

        val attendanceRef = database.getReference("attendance").child(selectedClass)

        for (i in 1 until binding.studentsTableLayout.childCount) {
            val tableRow = binding.studentsTableLayout.getChildAt(i) as TableRow
            val (studentInfo, radioGroup) = tableRow.tag as Pair<StudentInfo, RadioGroup>

            val attendanceStatus = if (radioGroup.checkedRadioButtonId == -1) {
                "Absent"
            } else {
                val selectedRadioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                selectedRadioButton.text.toString()
            }

            attendanceRef.child(studentInfo.uid).child(subjectId).child(selectedDate).setValue(attendanceStatus)
        }

        Toast.makeText(this, "Attendance updated successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun populateSpinner(spinner: Spinner, dataList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun populateSpinnerWithPairs(spinner: Spinner, dataList: List<Pair<String, String>>) {
        val adapter = object : ArrayAdapter<Pair<String, String>>(
            this,
            android.R.layout.simple_spinner_item,
            dataList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.text = getItem(position)?.first
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.text = getItem(position)?.first
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    data class StudentInfo(
        val uid: String,
        val roleNumber: String,
        val name: String,
        val attendanceStatus: String
    )
}