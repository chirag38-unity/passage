package com.tweener.passage.auth.firebase

import com.tweener.passage.auth.firebase.handler.FirebaseEmailPasswordHandler
import com.tweener.passage.auth.firebase.handler.FirebaseIdTokenHandler
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.AuthCredentialHandler
import com.tweener.passage.core.plugin.PassageAuthPlugin
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Firebase implementation of [PassageAuthPlugin].
 *
 * Provides authentication capabilities using Firebase Authentication.
 * Delegates credential-specific logic to [AuthCredentialHandler] instances
 * for email/password and ID token authentication.
 *
 * ### Usage
 * ```kotlin
 * val plugin = FirebaseAuthPlugin(Firebase.auth)
 * val core = PassageCore(
 *     authPlugin = plugin,
 *     userMapper = FirebaseUserMapper()
 * )
 * ```
 *
 * @param firebaseAuth The Firebase Authentication instance.
 */
class FirebaseAuthPlugin(
    private val firebaseAuth: FirebaseAuth,
) : PassageAuthPlugin {

    private val credentialHandlers: List<AuthCredentialHandler> = listOf(
        FirebaseEmailPasswordHandler(firebaseAuth),
        FirebaseIdTokenHandler(firebaseAuth),
    )

    override suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult> {
        val handler = credentialHandlers.firstOrNull { it.canHandle(credential) }
            ?: return Result.failure(IllegalArgumentException("No handler for credential type: ${credential::class.simpleName}"))
        return handler.authenticate(credential)
    }

    override suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult> {
        val handler = credentialHandlers.firstOrNull { it.canHandle(credential) }
            ?: return Result.failure(IllegalArgumentException("No handler for credential type: ${credential::class.simpleName}"))
        return handler.createUser(credential)
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> {
        val handler = credentialHandlers.firstOrNull { it.canHandle(credential) }
            ?: return Result.failure(IllegalArgumentException("No handler for credential type: ${credential::class.simpleName}"))
        return handler.reauthenticate(credential)
    }

    override fun getCurrentUser(): FirebaseUser? =
        firebaseAuth.currentUser

    @Suppress("USELESS_CAST")
    override fun observeAuthState(): Flow<Any?> =
        firebaseAuth.authStateChanged as Flow<Any?>

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> =
        firebaseAuth.currentUser
            ?.getIdToken(forceRefresh)
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No authenticated user found to retrieve ID token."))

    override suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()
    }
}
