package com.notes.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = appContext.getDatabasePath("notes.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
