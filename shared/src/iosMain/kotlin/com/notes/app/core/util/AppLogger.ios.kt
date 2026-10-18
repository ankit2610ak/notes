package com.notes.app.core.util

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun d(tag: String, message: String) {
        NSLog("[$tag] $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        NSLog("[$tag ERROR] $message: ${throwable?.message}")
    }
}
