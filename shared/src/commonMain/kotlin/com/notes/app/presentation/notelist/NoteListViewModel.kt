package com.notes.app.presentation.notelist

import com.notes.app.core.util.AppLogger
import com.notes.app.data.local.AppDatabase
import com.notes.app.data.local.entity.NoteEntity
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
    data class Success(val notes: List<NoteEntity>) : NoteUiState
    data class Error(val message: String) : NoteUiState
}

class NoteListViewModel(
    private val database: AppDatabase
) : BaseViewModel() {

    private val TAG = "NoteListViewModel"

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        AppLogger.d(TAG, "Initializing offline NoteListViewModel.")
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            AppLogger.d(TAG, "Observing notes stream from local database.")
            database.noteDao().observeNotes().collectLatest { notes ->
                AppLogger.d(TAG, "Loaded ${notes.size} notes from local DB.")
                _uiState.value = NoteUiState.Success(notes = notes)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveNote(noteId: String?, title: String, content: String) {
        viewModelScope.launch {
            val id = noteId ?: Uuid.random().toString()
            AppLogger.d(TAG, "Saving note id=$id with title='$title'")
            val note = NoteEntity(
                id = id,
                title = title,
                content = content,
                updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
            database.noteDao().upsertNote(note)
            AppLogger.d(TAG, "Note successfully saved locally with id=$id")
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            AppLogger.d(TAG, "Deleting note id=$noteId")
            database.noteDao().deleteNote(noteId)
            AppLogger.d(TAG, "Note id=$noteId deleted successfully.")
        }
    }
}
