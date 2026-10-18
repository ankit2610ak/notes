package com.notes.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

lateinit var appContext: Context

actual fun getDatabase(): AppDatabase {
    val dbFile = appContext.getDatabasePath("notes_offline.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
     .setQueryCoroutineContext(Dispatchers.IO)
     .fallbackToDestructiveMigration(true)
     .build()
}
