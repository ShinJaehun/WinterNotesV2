package com.shinjaehun.winternotesv2.model

import com.shinjaehun.winternotesv2.common.Result

interface IUserRepository {
    suspend fun getCurrentUser(): Result<Exception, User?>
    suspend fun signOutCurrentUser(): Result<Exception, Unit>
    suspend fun signInUser(email: String, password: String): Result<Exception, Unit>
}