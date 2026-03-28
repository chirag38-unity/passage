package com.tweener.passage.core.plugin

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult

/**
 * Handles authentication for a specific type of [AuthCredential].
 *
 * Plugins can decompose their authentication logic into multiple credential handlers,
 * each responsible for a specific credential type (e.g., email/password, ID token).
 *
 * This is an optional abstraction — plugins may implement all logic directly or delegate to handlers.
 */
interface AuthCredentialHandler {

    /**
     * Returns whether this handler can process the given credential.
     */
    fun canHandle(credential: AuthCredential): Boolean

    /**
     * Authenticates a user with the given credential.
     *
     * @param credential The authentication credential.
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    suspend fun authenticate(credential: AuthCredential): Result<PassageAuthResult>

    /**
     * Creates a new user with the given credential.
     *
     * @param credential The authentication credential.
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    suspend fun createUser(credential: AuthCredential): Result<PassageAuthResult>

    /**
     * Re-authenticates a user with the given credential.
     *
     * @param credential The authentication credential.
     * @return A [Result] indicating success or failure.
     */
    suspend fun reauthenticate(credential: AuthCredential): Result<Unit>
}
