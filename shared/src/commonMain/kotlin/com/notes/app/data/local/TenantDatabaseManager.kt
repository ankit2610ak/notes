package com.notes.app.data.local

import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TenantDatabaseManager(
    private val builderFactory: (userId: String) -> RoomDatabase.Builder<AppDatabase>
) {
    private val activeDatabases = mutableMapOf<String, AppDatabase>()
    private val mutex = Mutex()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _activeDatabase = MutableStateFlow<AppDatabase?>(null)
    val activeDatabase: StateFlow<AppDatabase?> = _activeDatabase.asStateFlow()

    suspend fun switchUser(userId: String): AppDatabase = mutex.withLock {
        if (_currentUserId.value == userId && _activeDatabase.value != null) {
            return@withLock _activeDatabase.value!!
        }

        _currentUserId.value = userId

        val db = activeDatabases.getOrPut(userId) {
            builderFactory(userId).build()
        }
        _activeDatabase.value = db
        return@withLock db
    }

    suspend fun clearSession() = mutex.withLock {
        _currentUserId.value = null
        _activeDatabase.value = null
        activeDatabases.values.forEach { 
            try { it.close() } catch (_: Exception) {}
        }
        activeDatabases.clear()
    }
}
