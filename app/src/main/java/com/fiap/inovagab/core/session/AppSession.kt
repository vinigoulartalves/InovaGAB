package com.fiap.inovagab.core.session

import com.fiap.inovagab.InovaGabApp

object AppSession {
    val manager: SessionManager
        get() = InovaGabApp.instance.sessionManager
}
