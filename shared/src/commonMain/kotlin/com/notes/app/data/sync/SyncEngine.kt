package com.notes.app.data.sync

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
    suspend fun synchronize(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotes(userId)
            for (note in unsyncedNotes) {
                val dto = NoteDto(
                    id = note.id,
                    userId = note.userId,
                    title = note.title,
                    content = note.content,
                    updatedAt = note.updatedAt,
                    isDeleted = note.isDeleted
                )
                val response = httpClient.post("$baseUrl/notes/sync") {
                    contentType(ContentType.Application.Json)
                    setBody(dto)
                }
                if (response.status.value in 200..299) {
                    val serverTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    noteDao.updateSyncStatus(note.id, userId, SyncStatus.SYNCED, serverTime)
                }
            }

            val remoteNotes: List<NoteDto> = httpClient.get("$baseUrl/notes?userId=$userId").body()
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
                    noteDao.upsertNote(remoteEntity)
                } else {
                    if (ConflictResolver.shouldRemoteOverwriteLocal(localNote, remoteEntity)) {
                        noteDao.upsertNote(remoteEntity)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
