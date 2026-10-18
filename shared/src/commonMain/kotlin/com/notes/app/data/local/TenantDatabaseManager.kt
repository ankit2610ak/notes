package com.notes.app.data.local

import androidx.room.RoomDatabase
import com.notes.app.core.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TenantDatabaseManager(
    private val builderFactory: (userId: String) -> RoomDatabase.Builder<AppDatabase>
) {
    private val TAG = "TenantDatabaseManager"
    private val activeDatabases = mutableMapOf<String, AppDatabase>()
    private val mutex = Mutex()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _activeDatabase = MutableStateFlow<AppDatabase?>(null)
    val activeDatabase: StateFlow<AppDatabase?> = _activeDatabase.asStateFlow()

    suspend fun switchUser(userId: String): AppDatabase = mutex.withLock {
        AppLogger.d(TAG, "Switching tenant session to userId: $userId")
        if (_currentUserId.value == userId && _activeDatabase.value != null) {
            AppLogger.d(TAG, "User $userId is already active. Reusing existing database instance.")
            return@withLock _activeDatabase.value!!
        }

        _currentUserId.value = userId

        val db = activeDatabases.getOrPut(userId) {
            AppLogger.d(TAG, "Building new Room database instance for tenant: $userId")
            builderFactory(userId).build()
        }
        _activeDatabase.value = db
        AppLogger.d(TAG, "Successfully switched active database to tenant: $userId")
        return@withLock db
    }

    suspend fun clearSession() = mutex.withLock {
        AppLogger.d(TAG, "Clearing all tenant sessions and closing database connections.")
        _currentUserId.value = null
        _activeDatabase.value = null
        activeDatabases.values.forEach { 
            try { it.close() } catch (e: Exception) {
                AppLogger.e(TAG, "Error closing database connection", e)
            }
        }
        activeDatabases.clear()
    }
}
