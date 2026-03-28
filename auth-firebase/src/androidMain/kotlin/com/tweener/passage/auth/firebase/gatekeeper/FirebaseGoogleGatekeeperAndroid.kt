package com.tweener.passage.auth.firebase.gatekeeper

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.auth.firebase.error.PassageActivityContextNotInitializedException
import com.tweener.passage.auth.firebase.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.auth.firebase.error.PassageGoogleGatekeeperUnknownCredentialException
import com.tweener.passage.auth.firebase.model.GoogleTokens
import com.tweener.passage.core.PassageViewBinder
import com.tweener.passage.core.model.PassageAuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider

/**
 * Android implementation of [FirebaseGoogleGatekeeper].
 *
 * Uses the Credential Manager API to retrieve Google credentials, with a legacy
 * GoogleSignIn fallback. Extracts ID tokens and authenticates with Firebase.
 *
 * @param serverClientId The server client ID for Google Sign-In.
 * @param firebaseAuth The Firebase Authentication instance.
 * @param applicationContext The Android application context.
 * @param viewBinder The view binder providing activity context and launcher.
 * @param useGoogleButtonFlow If true, uses the Google button flow.
 * @param filterByAuthorizedAccounts If true, filters by authorized accounts.
 * @param autoSelectEnabled If true, enables auto-selection.
 * @param maxRetries Maximum number of retry attempts.
 */
class FirebaseGoogleGatekeeperAndroid(
    serverClientId: String,
    private val firebaseAuth: FirebaseAuth,
    private val applicationContext: Context,
    private val viewBinder: PassageViewBinder,
    private val useGoogleButtonFlow: Boolean = false,
    private val filterByAuthorizedAccounts: Boolean = false,
    private val autoSelectEnabled: Boolean = true,
    private val maxRetries: Int = 3,
) : FirebaseGoogleGatekeeper(serverClientId = serverClientId) {

    private val credentialManager = CredentialManager.create(applicationContext)
    private val legacyGatekeeper = FirebaseGoogleLegacyGatekeeperAndroid(
        serverClientId = serverClientId,
        firebaseAuth = firebaseAuth,
        activityContext = { viewBinder.getActivityContext() },
        activityResultLauncher = { viewBinder.getLauncher() },
        activityResult = { viewBinder.getActivityResult() },
    )

    override suspend fun signIn(): Result<PassageAuthResult> = suspendCatching {
        var attempts = 0
        var lastThrowable: Throwable? = null
        var currentUseGoogleButtonFlow = useGoogleButtonFlow

        while (attempts <= maxRetries) {
            retrieveGoogleTokens(useGoogleButtonFlow = currentUseGoogleButtonFlow).fold(
                onSuccess = { googleTokens ->
                    val firebaseCredential = GoogleAuthProvider.credential(
                        idToken = googleTokens.idToken,
                        accessToken = googleTokens.accessToken,
                    )
                    val user = firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user
                        ?: throw PassageGatekeeperUnknownEntrantException()
                    return@suspendCatching PassageAuthResult(backendUser = user)
                },
                onFailure = { throwable ->
                    lastThrowable = throwable
                    println("Couldn't sign in the user. Attempt ${++attempts} of $maxRetries. Error:\n$throwable")

                    if (throwable is NoCredentialException) {
                        signOut()
                    }

                    if (throwable !is GetCredentialCancellationException) {
                        println("Attempt to sign in with Google Legacy provider.")
                        legacyGatekeeper.signIn().fold(
                            onSuccess = { return@suspendCatching it },
                            onFailure = { legacyThrowable ->
                                println("Couldn't sign in with Google Legacy provider. Error:\n$legacyThrowable")
                            }
                        )
                    }

                    currentUseGoogleButtonFlow = currentUseGoogleButtonFlow.not()

                    if (attempts >= maxRetries) {
                        throw lastThrowable ?: PassageGatekeeperUnknownEntrantException()
                    }
                },
            )
        }

        throw lastThrowable ?: PassageGatekeeperUnknownEntrantException()
    }

    override suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        legacyGatekeeper.signOut()
    }

    override suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens(useGoogleButtonFlow = useGoogleButtonFlow).fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(
                    idToken = googleTokens.idToken,
                    accessToken = googleTokens.accessToken,
                )
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable ->
                println("Couldn't re-authenticate the user. Error:\n$throwable")

                if (throwable is NoCredentialException) {
                    signOut()
                }

                if (throwable !is GetCredentialCancellationException) {
                    println("Attempt to re-authenticate with Google Legacy provider.")
                    legacyGatekeeper.reauthenticate().fold(
                        onSuccess = { return@suspendCatching it },
                        onFailure = { legacyThrowable ->
                            println("Couldn't re-authenticate with Google Legacy provider. Error:\n$legacyThrowable")
                        }
                    )
                }

                throw throwable
            },
        )
    }

    private suspend fun retrieveGoogleTokens(useGoogleButtonFlow: Boolean): Result<GoogleTokens> = suspendCatching {
        when (val credential = createCredentials(useGoogleButtonFlow = useGoogleButtonFlow)) {
            is CustomCredential -> {
                when (credential.type) {
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        println("Successful Google Sign In flow with idToken: $idToken")
                        GoogleTokens(idToken = idToken)
                    }
                    else -> {
                        println("Unexpected type of credential")
                        throw PassageGoogleGatekeeperUnknownCredentialException()
                    }
                }
            }
            else -> {
                println("Unexpected type of credential")
                throw PassageGoogleGatekeeperUnknownCredentialException()
            }
        }
    }.onFailure { throwable ->
        when (throwable) {
            is GoogleIdTokenParsingException -> println("Received an invalid google id token response. Error:\n$throwable")
            else -> println("Couldn't handle sign in response with Google gatekeeper. Error:\n$throwable")
        }
    }

    private suspend fun createCredentials(useGoogleButtonFlow: Boolean): Credential {
        viewBinder.getActivityContext() ?: throw PassageActivityContextNotInitializedException()

        val credentialOption = when (useGoogleButtonFlow) {
            true -> GetSignInWithGoogleOption.Builder(serverClientId).build()
            false -> GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(autoSelectEnabled)
                .build()
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(credentialOption)
            .build()

        return credentialManager.getCredential(
            request = request,
            context = viewBinder.getActivityContext()!!,
        ).credential
    }
}
