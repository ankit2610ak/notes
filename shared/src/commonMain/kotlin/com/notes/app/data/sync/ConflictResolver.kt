package com.notes.app.data.sync

import com.notes.app.data.local.entity.NoteEntity

object ConflictResolver {
    /**
     * Last-Write-Wins (LWW) with server timestamp arbitration.
     * Returns true if remote record should overwrite local record.
     */
    fun shouldRemoteOverwriteLocal(local: NoteEntity, remote: NoteEntity): Boolean {
        if (local.syncStatus == SyncStatus.DIRTY && local.updatedAt > remote.serverUpdatedAt) {
            return false
        }
        return remote.serverUpdatedAt >= local.updatedAt
    }
}
