package com.shinjaehun.winternotesv2.view.login

import android.content.Context

sealed class LoginEvent {
    data class OnLoginButtonClick(val email: String, val password: String): LoginEvent()
    object OnStart: LoginEvent()
}