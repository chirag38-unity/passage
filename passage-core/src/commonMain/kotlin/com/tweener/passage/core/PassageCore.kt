package com.tweener.passage.core

import androidx.compose.runtime.Composable
import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.Entrant
import com.tweener.passage.core.plugin.PassageAuthPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Main entry point for the modular Passage authentication system.
 *
 * **PassageCore** provides a backend-agnostic API for authentication operations.
 * It delegates to a [PassageAuthPlugin] for backend communication and uses a [PassageUserMapper]
 * to convert backend-specific user objects into the unified [Entrant] model.
 *
 * ### Architecture
 * ```
 * PassageCore
 *    ↓
 * PassageAuthPlugin (Firebase / Supabase / Custom)
 *    ↓
 * AuthCredentialHandler (optional per-credential-type handlers)
 *    ↓
 * Backend
 * ```
 *
 * ### Usage
 * ```kotlin
 * val core = PassageCore(
 *     authPlugin = FirebaseAuthPlugin(firebaseAuth),
 *     userMapper = FirebaseUserMapper()
 * )
 *
 * val result = core.signIn(AuthCredential.EmailPassword("user@example.com", "password"))
 * ```
 *
 * @param authPlugin The authentication backend plugin.
 * @param userMapper Maps backend user objects to [Entrant].
 */
class PassageCore(
    private val authPlugin: PassageAuthPlugin,
    private val userMapper: PassageUserMapper,
) {

    /**
     * Platform-specific lifecycle binder.
     *
     * On Android, provides access to the activity context and activity result launcher
     * needed for native OAuth flows (Google Sign-In, etc.).
     * On iOS, this is a no-op holder.
     *
     * Gatekeepers and plugins can access this to obtain platform-specific resources.
     */
    val viewBinder = PassageViewBinder()

    /**
     * Binds Passage to the current Composable view.
     *
     * This method is necessary when using native OAuth gatekeepers on Android,
     * as they require access to the current Activity-based context.
     * On iOS, this is a no-op.
     *
     * Must be called from a @Composable context.
     */
    @Composable
    fun bindToView() {
        viewBinder.bind()
    }

    /**
     * Signs in a user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] containing the authenticated [Entrant] on success, or an error on failure.
     */
    suspend fun signIn(credential: AuthCredential): Result<Entrant> =
        authPlugin.signIn(credential).map { userMapper.map(it.backendUser) }

    /**
     * Creates a new user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] containing the created [Entrant] on success, or an error on failure.
     */
    suspend fun signUp(credential: AuthCredential): Result<Entrant> =
        authPlugin.signUp(credential).map { userMapper.map(it.backendUser) }

    /**
     * Signs out the currently authenticated user.
     */
    suspend fun signOut() {
        authPlugin.signOut()
    }

    /**
     * Re-authenticates the currently authenticated user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] indicating success or failure.
     */
    suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        authPlugin.reauthenticate(credential)

    /**
     * Returns the currently authenticated user as an [Entrant], or null if no user is logged in.
     */
    fun getCurrentUser(): Entrant? =
        authPlugin.getCurrentUser()?.let { userMapper.map(it) }

    /**
     * Observes changes to the authenticated user as a [Flow] of [Entrant].
     *
     * @return A [Flow] emitting the current [Entrant], or null if no user is logged in.
     */
    fun observeAuthState(): Flow<Entrant?> =
        authPlugin.observeAuthState().map { user -> user?.let { userMapper.map(it) } }

    /**
     * Indicates whether a user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean =
        getCurrentUser() != null

    /**
     * Observes whether a user is logged in as a [Flow] of [Boolean].
     *
     * @return A [Flow] that emits true if a user is logged in, or false otherwise.
     */
    fun isUserLoggedInAsFlow(): Flow<Boolean> =
        observeAuthState().map { it != null }

    /**
     * Retrieves the ID token for the currently authenticated user.
     *
     * @param forceRefresh If true, forces a refresh of the ID token. Defaults to false.
     * @return A [Result] containing the ID token string, or an error.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> =
        authPlugin.getIdToken(forceRefresh)

    /**
     * Deletes the currently authenticated user.
     */
    suspend fun deleteCurrentUser() {
        authPlugin.deleteCurrentUser()
    }
}
