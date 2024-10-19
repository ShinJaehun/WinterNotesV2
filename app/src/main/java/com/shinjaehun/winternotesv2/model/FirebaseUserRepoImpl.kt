package com.shinjaehun.winternotesv2.model

import com.google.firebase.auth.FirebaseAuth
import com.shinjaehun.winternotesv2.common.Result
import com.shinjaehun.winternotesv2.common.awaitTaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseUserRepoImpl(
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
): IUserRepository {
    override suspend fun getCurrentUser(): Result<Exception, User?> {
        val firebaseUser = auth.currentUser

        return if (firebaseUser == null) {
            Result.build { null }
        } else {
            Result.build {
                User(
                    firebaseUser.uid,
                    firebaseUser.email ?: ""
                )
            }
        }
    }

    override suspend fun signOutCurrentUser(): Result<Exception, Unit> {
        return Result.build {
            auth.signOut()
        }
    }

    override suspend fun signInUser(email: String, password: String): Result<Exception, Unit>
        = withContext(Dispatchers.IO) {
        try {
            awaitTaskResult(auth.signInWithEmailAndPassword(email, password))
            Result.build { Unit }
        } catch (e: Exception) {
            Result.build { throw e }
        }
    }
}