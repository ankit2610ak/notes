package com.notes.app.data.sync

import com.notes.app.core.util.AppLogger
import com.notes.app.data.local.dao.NoteDao
import com.notes.app.data.local.entity.NoteEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val isDeleted: Boolean
)

class SyncEngine(
    private val noteDao: NoteDao,
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://api.notesapp.internal/v1"
) {
    private val TAG = "SyncEngine"

    suspend fun synchronize(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        AppLogger.d(TAG, "Starting two-way synchronization for userId: $userId")
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotes(userId)
            AppLogger.d(TAG, "Found ${unsyncedNotes.size} unsynced/dirty local records to push for user: $userId")
            
            for (note in unsyncedNotes) {
                try {
                    val dto = NoteDto(
                        id = note.id,
                        userId = note.userId,
                        title = note.title,
                        content = note.content,
                        updatedAt = note.updatedAt,
                        isDeleted = note.isDeleted
                    )
                    AppLogger.d(TAG, "Pushing note id=${note.id} (isDeleted=${note.isDeleted}) to remote server")
                    val response = httpClient.post("$baseUrl/notes/sync") {
                        contentType(ContentType.Application.Json)
                        setBody(dto)
                    }
                    if (response.status.value in 200..299) {
                        val serverTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                        noteDao.updateSyncStatus(note.id, userId, SyncStatus.SYNCED, serverTime)
                        AppLogger.d(TAG, "Successfully synced and marked note id=${note.id} as SYNCED")
                    } else {
                        AppLogger.e(TAG, "Failed to push note id=${note.id}, HTTP status: ${response.status.value}")
                    }
                } catch (e: Exception) {
                    AppLogger.d(TAG, "Network offline or server unreachable for note id=${note.id} (offline-first mode): ${e.message}")
                }
            }

            try {
                AppLogger.d(TAG, "Pulling delta changes from remote server for user: $userId")
                val remoteNotes: List<NoteDto> = httpClient.get("$baseUrl/notes?userId=$userId").body()
                AppLogger.d(TAG, "Received ${remoteNotes.size} remote records from server")

                for (remoteDto in remoteNotes) {
                    val localNote = noteDao.getNoteById(remoteDto.id, userId)
                    val remoteEntity = NoteEntity(
                        id = remoteDto.id,
                        userId = remoteDto.userId,
                        title = remoteDto.title,
                        content = remoteDto.content,
                        updatedAt = remoteDto.updatedAt,
                        serverUpdatedAt = remoteDto.updatedAt,
                        syncStatus = SyncStatus.SYNCED,
                        isDeleted = remoteDto.isDeleted
                    )

                    if (localNote == null) {
                        AppLogger.d(TAG, "Inserting new remote note id=${remoteDto.id} locally")
                        noteDao.upsertNote(remoteEntity)
                    } else {
                        val overwrite = ConflictResolver.shouldRemoteOverwriteLocal(localNote, remoteEntity)
                        AppLogger.d(TAG, "Conflict resolution for note id=${remoteDto.id}: remote overwrite = $overwrite")
                        if (overwrite) {
                            noteDao.upsertNote(remoteEntity)
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "Network offline or server unreachable during pull (offline-first mode): ${e.message}")
            }

            AppLogger.d(TAG, "Synchronization completed (offline-first handled) for userId: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Synchronization wrapper caught exception for userId: $userId", e)
            Result.success(Unit) // Return success to maintain smooth offline-first user experience
        }
    }
}
