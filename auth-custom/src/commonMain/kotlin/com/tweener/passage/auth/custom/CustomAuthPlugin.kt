package com.tweener.passage.auth.custom

import com.tweener.passage.auth.custom.model.CustomUser
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.PassageAuthPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Example implementation of [PassageAuthPlugin] for a custom HTTP authentication backend.
 *
 * Demonstrates how to build a custom auth plugin by delegating HTTP communication
 * to a [CustomAuthProvider] interface. This keeps the plugin free of HTTP client dependencies.
 *
 * ### Usage
 * ```kotlin
 * val plugin = CustomAuthPlugin(
 *     authProvider = object : CustomAuthProvider {
 *         // Implement using your preferred HTTP client
 *     }
 * )
 * val core = PassageCore(
 *     authPlugin = plugin,
 *     userMapper = CustomUserMapper()
 * )
 * ```
 *
 * @param authProvider The provider that handles actual HTTP API communication.
 */
class CustomAuthPlugin(
    private val authProvider: CustomAuthProvider,
) : PassageAuthPlugin {

    private val _currentUser = MutableStateFlow<CustomUser?>(null)

    override suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val user = when (credential) {
            is AuthCredential.EmailPassword -> authProvider.signIn(credential.email, credential.password)
            is AuthCredential.Custom -> authProvider.signInWithCustomData(credential.data)
            else -> throw IllegalArgumentException("Unsupported credential type for custom backend: ${credential::class.simpleName}")
        }
        _currentUser.value = user
        PassageAuthResult(backendUser = user)
    }

    override suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as? AuthCredential.EmailPassword
            ?: throw IllegalArgumentException("Custom backend sign-up requires EmailPassword credentials.")
        val user = authProvider.signUp(params.email, params.password)
        _currentUser.value = user
        PassageAuthResult(backendUser = user)
    }

    override suspend fun signOut() {
        authProvider.signOut()
        _currentUser.value = null
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        signIn(credential).map { }

    override fun getCurrentUser(): CustomUser? =
        _currentUser.value

    @Suppress("USELESS_CAST")
    override fun observeAuthState(): Flow<Any?> =
        _currentUser as Flow<Any?>

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("No authenticated user found to retrieve token."))
        return runCatching {
            if (forceRefresh) authProvider.refreshToken(user.id) else (user.token ?: authProvider.refreshToken(user.id))
        }
    }

    override suspend fun deleteCurrentUser() {
        val user = _currentUser.value ?: throw IllegalStateException("No authenticated user to delete.")
        authProvider.deleteUser(user.id)
        _currentUser.value = null
    }
}

/**
 * Provides the actual HTTP API communication for the custom backend.
 *
 * Implement this interface using your preferred HTTP client (Ktor, OkHttp, etc.)
 * to handle authentication operations against your custom API.
 */
interface CustomAuthProvider {

    /**
     * Signs in a user with email and password.
     *
     * @return The authenticated [CustomUser].
     */
    suspend fun signIn(email: String, password: String): CustomUser

    /**
     * Signs in a user with custom key-value data.
     *
     * @param data Custom authentication data.
     * @return The authenticated [CustomUser].
     */
    suspend fun signInWithCustomData(data: Map<String, String>): CustomUser

    /**
     * Creates a new user with email and password.
     *
     * @return The created [CustomUser].
     */
    suspend fun signUp(email: String, password: String): CustomUser

    /**
     * Signs out the current user.
     */
    suspend fun signOut()

    /**
     * Refreshes the access token for a user.
     *
     * @param userId The ID of the user.
     * @return The new access token string.
     */
    suspend fun refreshToken(userId: String): String

    /**
     * Deletes a user by ID.
     *
     * @param userId The ID of the user to delete.
     */
    suspend fun deleteUser(userId: String)
}
