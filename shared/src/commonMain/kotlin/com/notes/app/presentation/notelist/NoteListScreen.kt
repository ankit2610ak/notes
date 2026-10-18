package com.notes.app.presentation.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notes.app.data.local.entity.NoteEntity
import com.notes.app.presentation.components.AccountSwitcher

@Composable
fun NoteListScreen(
    currentUserId: String?,
    userDisplayName: String? = null,
    userEmail: String? = null,
    availableUsers: List<String>,
    uiState: NoteUiState,
    onUserSelected: (String) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onCreateNoteClick: () -> Unit,
    onDeleteNote: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Notes", style = MaterialTheme.typography.headlineMedium)
                        if (userDisplayName != null) {
                            Text("Profile: $userDisplayName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        if (userEmail != null) {
                            Text(userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    AccountSwitcher(
                        currentUserId = currentUserId,
                        availableUsers = availableUsers,
                        onUserSelected = onUserSelected
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentUserId != null) {
                FloatingActionButton(onClick = onCreateNoteClick) {
                    Text("+")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is NoteUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoteUiState.Success -> {
                    if (state.notes.isEmpty()) {
                        Text("No notes found. Tap '+' to create one.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            items(state.notes, key = { it.id }) { note ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNoteClick(note) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(note.title, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = { onDeleteNote(note.id) }) {
                                                Text("Delete", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is NoteUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
