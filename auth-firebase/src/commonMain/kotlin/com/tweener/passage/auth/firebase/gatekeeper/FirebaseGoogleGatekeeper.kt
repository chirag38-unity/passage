package com.tweener.passage.auth.firebase.gatekeeper

import com.tweener.passage.core.model.PassageAuthResult

/**
 * Abstract base class for handling Google Sign-In in the Firebase auth module.
 *
 * Platform-specific implementations handle the native OAuth UI flow,
 * extract ID tokens, and delegate to Firebase for backend authentication.
 *
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 */
abstract class FirebaseGoogleGatekeeper(
    protected val serverClientId: String,
) {

    /**
     * Signs in a user using Google Sign-In.
     *
     * Triggers the native Google Sign-In UI, extracts tokens, and authenticates with Firebase.
     *
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    abstract suspend fun signIn(): Result<PassageAuthResult>

    /**
     * Signs out the current Google user.
     */
    abstract suspend fun signOut()

    /**
     * Re-authenticates the currently authenticated user using Google Sign-In.
     *
     * @return A [Result] indicating success or failure.
     */
    abstract suspend fun reauthenticate(): Result<Unit>
}
