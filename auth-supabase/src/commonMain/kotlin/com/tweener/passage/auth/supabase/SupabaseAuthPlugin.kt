package com.tweener.passage.auth.supabase

import com.tweener.passage.auth.supabase.model.SupabaseUser
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.PassageAuthPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Supabase implementation of [PassageAuthPlugin].
 *
 * Provides authentication using Supabase's ID token-based authentication.
 * This plugin accepts a [SupabaseAuthProvider] to delegate actual HTTP communication,
 * keeping this module free of HTTP client dependencies.
 *
 * ### Usage
 * ```kotlin
 * val plugin = SupabaseAuthPlugin(
 *     authProvider = object : SupabaseAuthProvider {
 *         // Implement using your preferred HTTP client (Ktor, OkHttp, etc.)
 *     }
 * )
 * val core = PassageCore(
 *     authPlugin = plugin,
 *     userMapper = SupabaseUserMapper()
 * )
 * ```
 *
 * @param authProvider The provider that handles actual Supabase API communication.
 */
class SupabaseAuthPlugin(
    private val authProvider: SupabaseAuthProvider,
) : PassageAuthPlugin {

    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)

    override suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val user = when (credential) {
            is AuthCredential.EmailPassword -> authProvider.signInWithEmail(credential.email, credential.password)
            is AuthCredential.IdToken -> authProvider.signInWithIdToken(credential.idToken, credential.provider ?: "google")
            else -> throw IllegalArgumentException("Unsupported credential type for Supabase: ${credential::class.simpleName}")
        }
        _currentUser.value = user
        PassageAuthResult(backendUser = user)
    }

    override suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as? AuthCredential.EmailPassword
            ?: throw IllegalArgumentException("Supabase sign-up requires EmailPassword credentials.")
        val user = authProvider.signUpWithEmail(params.email, params.password)
        _currentUser.value = user
        PassageAuthResult(backendUser = user)
    }

    override suspend fun signOut() {
        authProvider.signOut()
        _currentUser.value = null
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        signIn(credential).map { }

    override fun getCurrentUser(): SupabaseUser? =
        _currentUser.value

    @Suppress("USELESS_CAST")
    override fun observeAuthState(): Flow<Any?> =
        _currentUser as Flow<Any?>

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> =
        runCatching { authProvider.getAccessToken(forceRefresh) }

    override suspend fun deleteCurrentUser() {
        val user = _currentUser.value ?: throw IllegalStateException("No authenticated user to delete.")
        authProvider.deleteUser(user.id)
        _currentUser.value = null
    }
}

/**
 * Provides the actual Supabase API communication.
 *
 * Implement this interface using your preferred HTTP client to handle
 * Supabase authentication operations.
 */
interface SupabaseAuthProvider {

    /**
     * Signs in a user with email and password.
     *
     * @return The authenticated [SupabaseUser].
     */
    suspend fun signInWithEmail(email: String, password: String): SupabaseUser

    /**
     * Signs in a user with a third-party ID token.
     *
     * @param idToken The ID token from the provider.
     * @param provider The provider name (e.g., "google", "apple").
     * @return The authenticated [SupabaseUser].
     */
    suspend fun signInWithIdToken(idToken: String, provider: String): SupabaseUser

    /**
     * Creates a new user with email and password.
     *
     * @return The created [SupabaseUser].
     */
    suspend fun signUpWithEmail(email: String, password: String): SupabaseUser

    /**
     * Signs out the current user.
     */
    suspend fun signOut()

    /**
     * Retrieves the current access token.
     *
     * @param forceRefresh Whether to force a token refresh.
     * @return The access token string.
     */
    suspend fun getAccessToken(forceRefresh: Boolean): String

    /**
     * Deletes a user by ID.
     *
     * @param userId The ID of the user to delete.
     */
    suspend fun deleteUser(userId: String)
}
