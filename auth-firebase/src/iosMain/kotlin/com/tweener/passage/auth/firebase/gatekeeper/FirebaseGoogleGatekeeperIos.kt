package com.tweener.passage.auth.firebase.gatekeeper

import cocoapods.GoogleSignIn.GIDSignIn
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.kmpkit.utils.safeLet
import com.tweener.passage.auth.firebase.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.auth.firebase.error.PassageGoogleGatekeeperException
import com.tweener.passage.auth.firebase.model.GoogleTokens
import com.tweener.passage.core.model.PassageAuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [FirebaseGoogleGatekeeper].
 *
 * Uses the Google Identity SDK (GIDSignIn) to perform Google Sign-In on iOS,
 * then authenticates with Firebase using the extracted tokens.
 *
 * @param firebaseAuth The Firebase Authentication instance.
 * @param serverClientId The server client ID for Google Sign-In.
 */
class FirebaseGoogleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
    serverClientId: String,
) : FirebaseGoogleGatekeeper(serverClientId = serverClientId) {

    override suspend fun signIn(): Result<PassageAuthResult> = suspendCatching {
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
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        println("Couldn't sign in the user with Google on iOS: $throwable")
    }

    override suspend fun signOut() {
        // Nothing to do here for iOS Google Sign-In
    }

    override suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens().fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(
                    idToken = googleTokens.idToken,
                    accessToken = googleTokens.accessToken,
                )
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        println("Couldn't re-authenticate the user with Google on iOS: $throwable")
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> = suspendCoroutine { continuation ->
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.let { rootViewController ->
                GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { authResult, error ->
                    error?.let { println("Couldn't sign in with Google on iOS! $error") }

                    when {
                        error != null -> continuation.resumeWithException(PassageGoogleGatekeeperException())

                        else -> {
                            safeLet(
                                authResult?.user?.idToken?.tokenString,
                                authResult?.user?.accessToken?.tokenString,
                            ) { idToken, accessToken ->
                                continuation.resume(Result.success(GoogleTokens(idToken = idToken, accessToken = accessToken)))
                            } ?: continuation.resumeWithException(PassageGoogleGatekeeperException())
                        }
                    }
                }
            }
            ?: continuation.resumeWithException(PassageGoogleGatekeeperException())
    }
}
