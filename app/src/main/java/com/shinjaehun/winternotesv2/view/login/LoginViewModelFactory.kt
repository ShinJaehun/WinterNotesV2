package com.shinjaehun.winternotesv2.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shinjaehun.winternotesv2.model.IUserRepository
import kotlinx.coroutines.Dispatchers

class LoginViewModelFactory(
    private val userRepo: IUserRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(userRepo, Dispatchers.Main) as T
    }
}