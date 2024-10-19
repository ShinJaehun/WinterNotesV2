package com.shinjaehun.winternotesv2.view.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shinjaehun.winternotesv2.common.BaseViewModel
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.model.IUserRepository
import com.shinjaehun.winternotesv2.model.User
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val TAG = "LoginViewModel"

class LoginViewModel(
    val repo: IUserRepository,
    uiContext: CoroutineContext
) : BaseViewModel<LoginEvent>(uiContext) {

    private val userState = MutableLiveData<User?>()
    val user: LiveData<User?> get() = userState

//    private val emailState = MutableLiveData<String?>()
//    val email: LiveData<String?> get() = emailState
//
//    private val passwordState = MutableLiveData<String?>()
//    val password: LiveData<String?> get() = passwordState

    private fun showErrorState() {
        Log.i(TAG, "showErrorState")
    }

    private fun showSignedInState() {
        Log.i(TAG, "showSignedInState")
    }

    private fun showSignedOutState() {
        Log.i(TAG, "showSignedOutState")
    }


    override fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnStart -> getUser()
            is LoginEvent.OnLoginButtonClick -> onLoginButtonClick(event.email, event.password)
        }
    }

    private fun getUser() = launch {
        val result = repo.getCurrentUser()
        when (result) {
            is Result.Value -> {
                userState.value = result.value
                if (result.value == null) showSignedOutState()
                else showSignedInState()
            }
            is Result.Error -> showErrorState()
        }
    }

    private fun onLoginButtonClick(email: String, password: String) {
        if (userState.value == null) {
            googleSignIn(email, password)
        } else googleSignOut()
    }

    private fun googleSignIn(email:String, password: String) {
        viewModelScope.launch {
            try {
                val result = repo.signInUser(email, password)
                when (result) {
                    is Result.Value -> getUser()
                    is Result.Error -> showErrorState()
                }
            } catch (e: Exception) {
                showErrorState()
            }
        }
    }

    private fun googleSignOut() = launch {
        val result = repo.signOutCurrentUser()
        when (result) {
            is Result.Value -> {
                userState.value = null
                showSignedOutState()
            }
            is Result.Error -> showErrorState()
        }
    }
}