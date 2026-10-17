package com.notes.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountSwitcher(
    currentUserId: String?,
    availableUsers: List<String>,
    onUserSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(8.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = currentUserId?.let { "User: $it" } ?: "Select Account")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableUsers.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user) },
                    onClick = {
                        expanded = false
                        onUserSelected(user)
                    }
                )
            }
        }
    }
}
