package com.notes.app.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseViewModel {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
