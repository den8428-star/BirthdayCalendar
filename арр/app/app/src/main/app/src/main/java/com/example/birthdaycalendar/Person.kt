package com.example.birthdaycalendar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val day: Int,
    val month: Int,
    val year: Int?,
    val relation: String?,
    val note: String?,
    val photoPath: String?,
    val remindDaysBefore: Int = 1
)
