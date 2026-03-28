package com.tweener.passage.auth.firebase.gatekeeper

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.auth.firebase.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.auth.firebase.model.GoogleTokens
import com.tweener.passage.core.model.PassageAuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Legacy Android Google Sign-In implementation using the deprecated GoogleSignIn API.
 *
 * Used as a fallback when the Credential Manager API fails.
 */
internal class FirebaseGoogleLegacyGatekeeperAndroid(
    private val serverClientId: String,
    private val firebaseAuth: FirebaseAuth,
    private val activityContext: () -> Context?,
    private val activityResultLauncher: () -> ManagedActivityResultLauncher<Intent, ActivityResult>?,
    private val activityResult: () -> ActivityResult?,
) {

    suspend fun signIn(): Result<PassageAuthResult> = suspendCatching {
        retrieveGoogleTokens().fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(
                    idToken = googleTokens.idToken,
                    accessToken = googleTokens.accessToken,
                )
                val user = firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user
                    ?: throw PassageGatekeeperUnknownEntrantException()
                PassageAuthResult(backendUser = user)
            },
            onFailure = { throwable -> throw throwable }
        )
    }

    suspend fun signOut() {
        try {
            getGoogleSignInClient().signOut()
        } catch (throwable: Throwable) {
            println("Google Legacy sign out failed with an unknown error: $throwable")
        }
    }

    suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens().fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(
                    idToken = googleTokens.idToken,
                    accessToken = googleTokens.accessToken,
                )
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable }
        )
    }

    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> = suspendCatching {
        try {
            activityResultLauncher.invoke()?.launch(getGoogleSignInClient().signInIntent)

            withContext(Dispatchers.Default) {
                while (activityResult.invoke() == null) yield()
            }

            val data: Intent? = activityResult.invoke()!!.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

            account.idToken
                ?.let { idToken -> GoogleTokens(idToken = idToken) }
                ?: throw Exception("Google idToken is null while signing in with Google Legacy provider.")
        } catch (throwable: Throwable) {
            when (throwable) {
                is ApiException -> println("Google Legacy sign in failed with ApiException: ${throwable.statusCode}")
                else -> println("Google Legacy sign in failed with an unknown error: $throwable")
            }
            throw throwable
        }
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(activityContext.invoke()!!, googleSignInOptions)
    }
}
