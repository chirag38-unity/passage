package com.tweener.passage.auth.firebase.gatekeeper

import com.tweener.passage.core.model.PassageAuthResult

/**
 * Abstract base class for handling Apple Sign-In in the Firebase auth module.
 *
 * Platform-specific implementations handle the native Apple Sign-In flow
 * and delegate to Firebase for backend authentication.
 */
abstract class FirebaseAppleGatekeeper {

    /**
     * Signs in a user using Apple Sign-In.
     *
     * @return A [Result] containing the [PassageAuthResult] on success, or an error on failure.
     */
    abstract suspend fun signIn(): Result<PassageAuthResult>

    /**
     * Signs out the current Apple user.
     */
    abstract suspend fun signOut()
}
