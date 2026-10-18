# Notes - Kotlin Multiplatform (KMP) Offline-First App

A cross-platform (Android & iOS) offline-first note-saving application built with modern Kotlin Multiplatform, Compose Multiplatform, and AndroidX Room KMP following Clean Architecture principles.

## 🚀 Tech Stack
- **Kotlin Multiplatform (KMP)**: Shared domain, data, and presentation logic.
- **Compose Multiplatform**: Declarative cross-platform UI adhering to Material 3 guidelines.
- **AndroidX Room KMP**: Local offline SQLite database with bundled SQLite driver.
- **Kotlinx Coroutines & Serialization**: Asynchronous execution and data serialization.

---

## 📊 System Diagrams & Architecture

### 1. Use Case Diagram
```mermaid
graph TD
    User -->|Open App| ViewNotes[View Notes List]
    User -->|Tap '+'| CreateNote[Create New Note]
    User -->|Tap Existing Note| EditNote[Edit Existing Note]
    User -->|Tap Delete| DeleteNote[Delete Note]
    ViewNotes --> LocalDB[(Room Database)]
    CreateNote --> LocalDB
    EditNote --> LocalDB
    DeleteNote --> LocalDB
```

### 2. Class Diagram
```mermaid
classDiagram
    class MainActivity {
        - AppDatabase database
        - NoteListViewModel viewModel
        + onCreate(Bundle)
    }

    class NoteListViewModel {
        - AppDatabase database
        - StateFlow<NoteUiState> uiState
        + saveNote(noteId, title, content)
        + deleteNote(noteId)
    }

    class AppDatabase {
        <<abstract>>
        + noteDao() NoteDao
    }

    class NoteDao {
        <<interface>>
        + observeNotes() Flow<List<NoteEntity>>
        + upsertNote(note)
        + deleteNote(noteId)
    }

    class NoteEntity {
        + String id
        + String title
        + String content
        + Long updatedAt
    }

    MainActivity --> NoteListViewModel : initializes
    NoteListViewModel --> AppDatabase : queries
    AppDatabase --> NoteDao : provides DAO
    NoteDao --> NoteEntity : persists
```

### 3. Sequence Diagram (Note Creation & Editing Flow)
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Screen as NoteEditScreen
    participant VM as NoteListViewModel
    participant DAO as NoteDao
    participant DB as AppDatabase (SQLite)

    User->>Screen: Enter title & content, click Save
    Screen->>VM: saveNote(noteId, title, content)
    VM->>DAO: upsertNote(NoteEntity)
    DAO->>DB: INSERT OR REPLACE INTO notes
    DB-->>DAO: Success
    DAO-->>VM: Flow emits updated notes list
    VM-->>Screen: UIState.Success(notes) updated
    Screen-->>User: Return to NoteListScreen showing saved note
```

---

## 📂 Project Structure
- `shared/src/commonMain/kotlin/com/notes/app/data/local`: Room Database, Entity, and DAO definitions.
- `shared/src/commonMain/kotlin/com/notes/app/presentation`: ViewModels and Compose Multiplatform UI (`NoteListScreen`, `NoteEditScreen`).
- `androidMain` & `iosMain`: Platform-specific SQLite driver implementations.
- `.github/workflows`: Automated CI/CD workflows for building and testing.
