package com.example.birthdaycalendar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonDao {
    @Query("SELECT * FROM people")
    suspend fun getAll(): List<Person>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): Person?

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)
}
