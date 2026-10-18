package com.notes.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.notes.app.data.local.TenantDatabaseManager
import com.notes.app.data.local.appContext
import com.notes.app.data.local.getDatabaseBuilder
import com.notes.app.data.sync.SyncEngine
import com.notes.app.presentation.login.LoginScreen
import com.notes.app.presentation.notelist.NoteListScreen
import com.notes.app.presentation.notelist.NoteListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val tenantDatabaseManager by lazy {
        appContext = applicationContext
        TenantDatabaseManager { _ ->
            getDatabaseBuilder()
        }
    }

    private val syncEngine by lazy {
        SyncEngine(
            noteDao = tenantDatabaseManager.switchUser("default_user").noteDao(),
            httpClient = httpClient
        )
    }

    private val viewModel by lazy {
        NoteListViewModel(
            tenantDatabaseManager = tenantDatabaseManager,
            syncEngine = syncEngine
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        
        setContent {
            val scope = rememberCoroutineScope()
            var currentUserId by remember { mutableStateOf<String?>(null) }
            val availableUsers = listOf("user_1", "user_2", "default_user")
            val uiState by viewModel.uiState.collectAsState()

            if (currentUserId == null) {
                LoginScreen(
                    onGoogleSignInClick = {
                        scope.launch {
                            currentUserId = "user_1"
                            tenantDatabaseManager.switchUser("user_1")
                        }
                    }
                )
            } else {
                NoteListScreen(
                    currentUserId = currentUserId,
                    availableUsers = availableUsers,
                    uiState = uiState,
                    onUserSelected = { selectedUser ->
                        scope.launch {
                            currentUserId = selectedUser
                            tenantDatabaseManager.switchUser(selectedUser)
                        }
                    },
                    onNoteClick = { _ ->
                        // Handle note click
                    },
                    onCreateNoteClick = {
                        currentUserId?.let { userId ->
                            viewModel.createNote(userId, "New Note", "Content goes here...")
                        }
                    },
                    onDeleteNote = { noteId ->
                        currentUserId?.let { userId ->
                            viewModel.deleteNote(userId, noteId)
                        }
                    }
                )
            }
        }
    }
}
