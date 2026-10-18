package com.notes.app.presentation.notelist

import com.notes.app.core.util.AppLogger
import com.notes.app.data.local.entity.NoteEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NoteListViewModelTest {

    @Test
    fun testNoteEntityInitializationAndProperties() {
        AppLogger.d("NoteListViewModelTest", "Running verbose unit test for NoteEntity properties")
        val entity = NoteEntity(
            id = "uuid-1234",
            title = "Kotlin Multiplatform",
            content = "Offline-first architecture with Room KMP",
            updatedAt = 10000L
        )

        assertNotNull(entity)
        assertEquals("uuid-1234", entity.id)
        assertEquals("Kotlin Multiplatform", entity.title)
        assertEquals("Offline-first architecture with Room KMP", entity.content)
        assertEquals(10000L, entity.updatedAt)
    }

    @Test
    fun testNoteUiStateSuccessEmission() {
        AppLogger.d("NoteListViewModelTest", "Running verbose unit test for NoteUiState.Success")
        val sampleNotes = listOf(
            NoteEntity(id = "1", title = "Note 1", content = "Content 1", updatedAt = 100L),
            NoteEntity(id = "2", title = "Note 2", content = "Content 2", updatedAt = 200L)
        )
        val state = NoteUiState.Success(notes = sampleNotes)

        assertEquals(2, state.notes.size)
        assertEquals("Note 1", state.notes[0].title)
    }
}
