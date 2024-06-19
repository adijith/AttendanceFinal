package com.example.student

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for the RecyclerView
class SubjectAdapter(private val subjectInfoList: List<Triple<String, String, Float>>)
    : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    // ViewHolder class to hold references to each item's views
    class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
    }

    // Called when RecyclerView needs a new ViewHolder of the given type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        // Inflate the item layout (subject_box.xml)
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.subject_box, parent, false)
        return SubjectViewHolder(itemView)
    }

    // Called to bind the data to the ViewHolder
    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        // Get the data for the current position
        val (subjectId, subjectName, attendancePercentage) = subjectInfoList[position]

        // Set the text for the TextView in the item layout
        holder.subjectTextView.text = "$subjectName\nID: $subjectId\nAttendance: ${String.format("%.2f", attendancePercentage)}%"

        // Set the tag for the view holder (useful for click handling)
        holder.itemView.tag = subjectId

        holder.itemView.setOnClickListener {
            // Handle the click event (e.g., navigate to detailed attendance view)
            val intent = Intent(holder.itemView.context, ViewSubAttendanceActivity::class.java).apply {
                putExtra("subjectId", subjectId)
                putExtra("subjectName", subjectName) // Optional: pass subjectName if needed in ViewSubAttendanceActivity
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    // Returns the total number of items in the dataset
    override fun getItemCount(): Int {
        return subjectInfoList.size
    }
}
