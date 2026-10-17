# Notes - Kotlin Multiplatform (KMP) App

An offline-first, cross-platform (Android & iOS) note-taking application built with modern Kotlin Multiplatform, Compose Multiplatform, AndroidX Room KMP, Ktor Client, and Koin Dependency Injection following Clean Architecture principles.

## 🚀 Tech Stack
- **Kotlin Multiplatform (KMP)**: Shared domain, data, and presentation business logic.
- **Compose Multiplatform**: Declarative cross-platform UI.
- **AndroidX Room KMP**: Local offline-first SQLite database with bundled SQLite driver.
- **Ktor Client**: Network communication layer.
- **Koin**: Lightweight dependency injection framework.
- **Kotlinx Coroutines & Serialization**: Asynchronous execution and data serialization.

## 📂 Project Structure & Architecture
The project strictly follows **Clean Architecture** principles within the multiplatform module:
- `commonMain/kotlin/com/notes/app/domain`: Business models, use cases, and repository interfaces.
- `commonMain/kotlin/com/notes/app/data`: Local (Room SQLite database) and remote (Ktor API) implementations.
- `commonMain/kotlin/com/notes/app/presentation`: ViewModels and UI state management.
- `androidMain` & `iosMain`: Platform-specific entry points and expect/actual SQLite database drivers.

## 🛠️ Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/ankit2610ak/notes.git
   ```
2. Open the project in Android Studio (Ladybug or newer) or Xcode for iOS development.
3. Build and run the Android app via Gradle (`:app:assembleDebug`) or execute tests.
