package com.tweener.passage.core.plugin

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import kotlinx.coroutines.flow.Flow

/**
 * Plugin interface for authentication backends.
 *
 * Each authentication backend (Firebase, Supabase, custom HTTP, etc.) implements this interface
 * to provide authentication capabilities to [com.tweener.passage.core.PassageCore].
 *
 * The plugin returns backend-specific user objects which are then mapped to [com.tweener.passage.core.model.Entrant]
 * by [com.tweener.passage.core.mapper.PassageUserMapper].
 */
interface PassageAuthPlugin {

    /**
     * Signs in a user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult>

    /**
     * Creates a new user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult>

    /**
     * Signs out the currently authenticated user.
     */
    suspend fun signOut()

    /**
     * Re-authenticates the currently authenticated user with the given credentials.
     *
     * @param credential The authentication credentials.
     * @return A [Result] indicating success or failure.
     */
    suspend fun reauthenticate(credential: AuthCredential): Result<Unit>

    /**
     * Returns the currently authenticated backend user, or null if no user is logged in.
     */
    fun getCurrentUser(): Any?

    /**
     * Observes changes to the authenticated backend user as a [Flow].
     *
     * @return A [Flow] emitting the current backend user, or null if no user is logged in.
     */
    fun observeAuthState(): Flow<Any?>

    /**
     * Retrieves the ID token for the currently authenticated user.
     *
     * @param forceRefresh If true, forces a refresh of the ID token.
     * @return A [Result] containing the ID token string, or an error.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String>

    /**
     * Deletes the currently authenticated user.
     */
    suspend fun deleteCurrentUser()
}
