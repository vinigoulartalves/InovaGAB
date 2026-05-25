package com.fiap.inovagab.core.session

import com.fiap.inovagab.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
    }

    val isLogged: Boolean
        get() = _currentUser.value != null
}
