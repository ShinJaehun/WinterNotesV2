package com.shinjaehun.winternotesv2.view.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.FirebaseApp
import com.shinjaehun.winternotesv2.model.FirebaseUserRepoImpl
import com.shinjaehun.winternotesv2.model.IUserRepository

class LoginInjector(application: Application): AndroidViewModel(application) {

    init {
        FirebaseApp.initializeApp(application)
    }

    private fun getUserRepository(): IUserRepository {
        return FirebaseUserRepoImpl()
    }

    fun provideLoginViewModelFactory(): LoginViewModelFactory =
        LoginViewModelFactory(getUserRepository())
}