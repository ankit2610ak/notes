package com.notes.app.domain.auth

data class GoogleUser(
    val userId: String,
    val email: String,
    val displayName: String
)

interface AuthService {
    suspend fun signInWithGoogle(): Result<GoogleUser>
    suspend fun signOut()
}
