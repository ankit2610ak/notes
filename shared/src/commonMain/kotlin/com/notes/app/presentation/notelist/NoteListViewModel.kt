package com.notes.app.presentation.notelist

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

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        observeActiveTenant()
    }

    private fun observeActiveTenant() {
        viewModelScope.launch {
            tenantDatabaseManager.activeDatabase.collectLatest { db ->
                val userId = tenantDatabaseManager.currentUserId.value
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
            db.noteDao().observeNotesByUser(userId).collectLatest { notes ->
                _uiState.value = NoteUiState.Success(notes = notes)
            }
        }
    }

    fun triggerSync(userId: String) {
        viewModelScope.launch {
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
            triggerSync(userId)
        }
    }

    fun deleteNote(userId: String, noteId: String) {
        viewModelScope.launch {
            val db = tenantDatabaseManager.activeDatabase.value ?: tenantDatabaseManager.switchUser(userId)
            db.noteDao().softDeleteNote(noteId, userId)
            triggerSync(userId)
        }
    }
}
