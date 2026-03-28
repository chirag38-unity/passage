package com.tweener.passage.auth.firebase.handler

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.AuthCredentialHandler
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider

/**
 * Handles [AuthCredential.IdToken] authentication via Firebase.
 *
 * Supports sign-in using provider ID tokens (e.g., Google Sign-In).
 *
 * @param firebaseAuth The Firebase Authentication instance.
 */
class FirebaseIdTokenHandler(
    private val firebaseAuth: FirebaseAuth,
) : AuthCredentialHandler {

    override fun canHandle(credential: AuthCredential): Boolean =
        credential is AuthCredential.IdToken

    override suspend fun authenticate(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as AuthCredential.IdToken
        val firebaseCredential = GoogleAuthProvider.credential(params.idToken, params.accessToken)
        val result = firebaseAuth.signInWithCredential(firebaseCredential)
        val user = result.user ?: throw IllegalStateException("Firebase ID token sign-in succeeded but no user was returned.")
        PassageAuthResult(backendUser = user)
    }

    override suspend fun createUser(credential: AuthCredential): Result<PassageAuthResult> =
        authenticate(credential) // ID token-based flows don't distinguish sign-in from sign-up

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> = runCatching {
        val params = credential as AuthCredential.IdToken
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user to re-authenticate.")
        val firebaseCredential = GoogleAuthProvider.credential(params.idToken, params.accessToken)
        currentUser.reauthenticate(firebaseCredential)
    }
}
