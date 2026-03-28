package com.tweener.passage.auth.firebase.handler

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.AuthCredentialHandler
import dev.gitlive.firebase.auth.FirebaseAuth

/**
 * Handles [AuthCredential.EmailPassword] authentication via Firebase.
 *
 * Supports sign-in, sign-up, and re-authentication with email and password.
 *
 * @param firebaseAuth The Firebase Authentication instance.
 */
class FirebaseEmailPasswordHandler(
    private val firebaseAuth: FirebaseAuth,
) : AuthCredentialHandler {

    override fun canHandle(credential: AuthCredential): Boolean =
        credential is AuthCredential.EmailPassword

    override suspend fun authenticate(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as AuthCredential.EmailPassword
        val result = firebaseAuth.signInWithEmailAndPassword(params.email, params.password)
        val user = result.user ?: throw IllegalStateException("Firebase sign-in succeeded but no user was returned.")
        PassageAuthResult(backendUser = user)
    }

    override suspend fun createUser(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as AuthCredential.EmailPassword
        val result = firebaseAuth.createUserWithEmailAndPassword(params.email, params.password)
        val user = result.user ?: throw IllegalStateException("Firebase sign-up succeeded but no user was returned.")
        PassageAuthResult(backendUser = user)
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> = runCatching {
        val params = credential as AuthCredential.EmailPassword
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user to re-authenticate.")
        val emailCredential = dev.gitlive.firebase.auth.EmailAuthProvider.credential(params.email, params.password)
        currentUser.reauthenticate(emailCredential)
    }
}
