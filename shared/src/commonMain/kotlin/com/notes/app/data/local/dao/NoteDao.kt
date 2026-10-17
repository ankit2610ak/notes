package com.notes.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.notes.app.data.local.entity.NoteEntity
import com.notes.app.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeNotesByUser(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId AND userId = :userId LIMIT 1")
    suspend fun getNoteById(noteId: String, userId: String): NoteEntity?

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getUnsyncedNotes(userId: String): List<NoteEntity>

    @Query("UPDATE notes SET isDeleted = 1, syncStatus = :newStatus, updatedAt = :currentTime WHERE id = :noteId AND userId = :userId")
    suspend fun softDeleteNote(noteId: String, userId: String, newStatus: SyncStatus = SyncStatus.DIRTY, currentTime: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds())

    @Query("DELETE FROM notes WHERE id = :noteId AND userId = :userId")
    suspend fun hardDeleteNote(noteId: String, userId: String)

    @Query("UPDATE notes SET syncStatus = :status, serverUpdatedAt = :serverTime WHERE id = :noteId AND userId = :userId")
    suspend fun updateSyncStatus(noteId: String, userId: String, status: SyncStatus, serverTime: Long)
}
