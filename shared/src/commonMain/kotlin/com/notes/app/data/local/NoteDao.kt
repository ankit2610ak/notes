package com.notes.app.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT 1")
    suspend fun ping(): Int
}
