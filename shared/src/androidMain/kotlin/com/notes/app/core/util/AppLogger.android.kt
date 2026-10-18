package com.notes.app.core.util

import android.util.Log

actual object AppLogger {
    actual fun d(tag: String, message: String) {
        try {
            Log.d(tag, message)
        } catch (e: RuntimeException) {
            println("[$tag] $message")
        }
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        try {
            Log.e(tag, message, throwable)
        } catch (e: RuntimeException) {
            println("[$tag ERROR] $message: ${throwable?.message}")
        }
    }
}
