package com.notes.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notes.app.data.sync.SyncStatus

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val serverUpdatedAt: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.DIRTY,
    val isDeleted: Boolean = false
)
