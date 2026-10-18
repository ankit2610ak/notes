package com.notes.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.notes.app.data.local.appContext
import com.notes.app.data.local.getDatabase
import com.notes.app.data.local.entity.NoteEntity
import com.notes.app.presentation.notedetail.NoteEditScreen
import com.notes.app.presentation.notelist.NoteListScreen
import com.notes.app.presentation.notelist.NoteListViewModel

sealed interface AppScreen {
    data object NoteList : AppScreen
    data class NoteEdit(val note: NoteEntity? = null) : AppScreen
}

class MainActivity : ComponentActivity() {

    private val database by lazy {
        appContext = applicationContext
        getDatabase()
    }

    private val viewModel by lazy {
        NoteListViewModel(database = database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext

        setContent {
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.NoteList) }
            val uiState by viewModel.uiState.collectAsState()

            when (val screen = currentScreen) {
                is AppScreen.NoteList -> {
                    NoteListScreen(
                        uiState = uiState,
                        onNoteClick = { note ->
                            currentScreen = AppScreen.NoteEdit(note)
                        },
                        onCreateNoteClick = {
                            currentScreen = AppScreen.NoteEdit(null)
                        },
                        onDeleteNote = { noteId ->
                            viewModel.deleteNote(noteId)
                        }
                    )
                }
                is AppScreen.NoteEdit -> {
                    NoteEditScreen(
                        initialTitle = screen.note?.title ?: "",
                        initialContent = screen.note?.content ?: "",
                        onSave = { title, content ->
                            viewModel.saveNote(screen.note?.id, title, content)
                            currentScreen = AppScreen.NoteList
                        },
                        onBack = {
                            currentScreen = AppScreen.NoteList
                        }
                    )
                }
            }
        }
    }
}
