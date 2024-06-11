package com.example.teacher

data class Student(
    val StudentId: String,
    val email: String,
    val name: String,
    val sclass:String,
    val active: Boolean = true // Set active to true by default
){

}
