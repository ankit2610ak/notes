package com.notes.app.domain.model

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
