package com.tweener.passage.firebase.gatekeeper.google

import com.tweener.passage.core.gatekeeper.PassageGatekeeper

internal abstract class PassageGoogleGatekeeper(
    protected val serverClientId: String,
) : PassageGatekeeper<Unit>() {
    abstract suspend fun reauthenticate(): Result<Unit>
}
