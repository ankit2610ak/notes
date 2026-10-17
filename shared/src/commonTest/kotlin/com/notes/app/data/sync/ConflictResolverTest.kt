package com.notes.app.data.sync

import com.notes.app.data.local.entity.NoteEntity
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConflictResolverTest {

    @Test
    fun testRemoteOverwritesLocalWhenServerTimeIsNewer() {
        val local = NoteEntity(
            id = "1",
            userId = "user_1",
            title = "Local",
            content = "Content",
            updatedAt = 100L,
            serverUpdatedAt = 50L,
            syncStatus = SyncStatus.SYNCED
        )
        val remote = NoteEntity(
            id = "1",
            userId = "user_1",
            title = "Remote",
            content = "Content",
            updatedAt = 150L,
            serverUpdatedAt = 150L,
            syncStatus = SyncStatus.SYNCED
        )

        assertTrue(ConflictResolver.shouldRemoteOverwriteLocal(local, remote))
    }

    @Test
    fun testLocalKeptWhenDirtyAndNewerThanServer() {
        val local = NoteEntity(
            id = "1",
            userId = "user_1",
            title = "Local Unsynced Edit",
            content = "Content",
            updatedAt = 200L,
            serverUpdatedAt = 50L,
            syncStatus = SyncStatus.DIRTY
        )
        val remote = NoteEntity(
            id = "1",
            userId = "user_1",
            title = "Remote Old",
            content = "Content",
            updatedAt = 100L,
            serverUpdatedAt = 100L,
            syncStatus = SyncStatus.SYNCED
        )

        assertFalse(ConflictResolver.shouldRemoteOverwriteLocal(local, remote))
    }
}
