package com.tweener.passage.auth.firebase.gatekeeper

import com.tweener.passage.auth.firebase.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.core.model.PassageAuthResult

/**
 * Android implementation of [FirebaseAppleGatekeeper].
 *
 * Apple Sign-In is not natively supported on Android. This implementation
 * throws [PassageGatekeeperNotImplementedException] for the `signIn` method.
 */
class FirebaseAppleGatekeeperAndroid : FirebaseAppleGatekeeper() {

    override suspend fun signIn(): Result<PassageAuthResult> =
        Result.failure(PassageGatekeeperNotImplementedException(gatekeeper = "Apple", platform = "Android"))

    override suspend fun signOut() {
        // Nothing to do here
    }
}
