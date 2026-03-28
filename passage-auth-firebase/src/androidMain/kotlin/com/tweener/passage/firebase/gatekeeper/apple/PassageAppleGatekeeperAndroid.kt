package com.tweener.passage.firebase.gatekeeper.apple

import com.tweener.kmpkit.Platform
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.core.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.core.model.Entrant
import com.tweener.passage.core.model.GatekeeperType

internal class PassageAppleGatekeeperAndroid : PassageAppleGatekeeper() {

    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        throw PassageGatekeeperNotImplementedException(gatekeeper = GatekeeperType.APPLE, platform = Platform.ANDROID)
    }

    override suspend fun signOut() {
        // Nothing to do here
    }
}
