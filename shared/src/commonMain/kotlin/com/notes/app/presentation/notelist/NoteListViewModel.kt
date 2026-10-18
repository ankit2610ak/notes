package com.notes.app.presentation.notelist

import com.notes.app.core.util.AppLogger
import com.notes.app.data.local.AppDatabase
import com.notes.app.data.local.TenantDatabaseManager
import com.notes.app.data.local.entity.NoteEntity
import com.notes.app.data.sync.SyncEngine
import com.notes.app.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface NoteUiState {
    data object Loading : NoteUiState
    data class Success(val notes: List<NoteEntity>, val isSyncing: Boolean = false) : NoteUiState
    data class Error(val message: String) : NoteUiState
}

class NoteListViewModel(
    private val tenantDatabaseManager: TenantDatabaseManager,
    private val syncEngine: SyncEngine
) : BaseViewModel() {

    private val TAG = "NoteListViewModel"

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        AppLogger.d(TAG, "Initializing NoteListViewModel and observing active tenant.")
        observeActiveTenant()
    }

    private fun observeActiveTenant() {
        viewModelScope.launch {
            tenantDatabaseManager.activeDatabase.collectLatest { db ->
                val userId = tenantDatabaseManager.currentUserId.value
                AppLogger.d(TAG, "Active database emission received. userId=$userId, dbIsNull=${db == null}")
                if (db == null || userId == null) {
                    _uiState.value = NoteUiState.Success(emptyList())
                } else {
                    observeNotes(db, userId)
                    triggerSync(userId)
                }
            }
        }
    }

    private fun observeNotes(db: AppDatabase, userId: String) {
        viewModelScope.launch {
            AppLogger.d(TAG, "Observing notes stream from database for userId=$userId")
            db.noteDao().observeNotesByUser(userId).collectLatest { notes ->
                AppLogger.d(TAG, "Received ${notes.size} notes from DAO flow for userId=$userId")
                _uiState.value = NoteUiState.Success(notes = notes)
            }
        }
    }

    fun triggerSync(userId: String) {
        viewModelScope.launch {
            AppLogger.d(TAG, "Triggering manual/automatic sync for userId=$userId")
            val currentState = _uiState.value
            if (currentState is NoteUiState.Success) {
                _uiState.value = currentState.copy(isSyncing = true)
            }
            syncEngine.synchronize(userId)
            if (currentState is NoteUiState.Success) {
                _uiState.value = currentState.copy(isSyncing = false)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createNote(userId: String, title: String, content: String) {
        viewModelScope.launch {
            AppLogger.d(TAG, "Creating new note for userId=$userId with title='$title'")
            val db = tenantDatabaseManager.activeDatabase.value ?: tenantDatabaseManager.switchUser(userId)
            val newNote = NoteEntity(
                id = Uuid.random().toString(),
                userId = userId,
                title = title,
                content = content,
                updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                syncStatus = com.notes.app.data.sync.SyncStatus.DIRTY,
                isDeleted = false
            )
            db.noteDao().upsertNote(newNote)
            AppLogger.d(TAG, "Note created and upserted locally with id=${newNote.id}")
            triggerSync(userId)
        }
    }

    fun deleteNote(userId: String, noteId: String) {
        viewModelScope.launch {
            AppLogger.d(TAG, "Soft deleting note id=$noteId for userId=$userId")
            val db = tenantDatabaseManager.activeDatabase.value ?: tenantDatabaseManager.switchUser(userId)
            db.noteDao().softDeleteNote(noteId, userId)
            AppLogger.d(TAG, "Note id=$noteId marked as deleted locally.")
            triggerSync(userId)
        }
    }
}
