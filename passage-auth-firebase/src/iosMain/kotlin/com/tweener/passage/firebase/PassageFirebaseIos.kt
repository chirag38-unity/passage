package com.tweener.passage.firebase

import androidx.compose.runtime.Composable
import com.tweener.passage.firebase.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.firebase.gatekeeper.apple.PassageAppleGatekeeperIos
import com.tweener.passage.firebase.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.firebase.gatekeeper.google.PassageGoogleGatekeeperIos
import com.tweener.passage.firebase.model.AppleGatekeeperConfiguration
import com.tweener.passage.firebase.model.GoogleGatekeeperConfiguration
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.initialize

class PassageFirebaseIos : PassageFirebase() {

    @Composable
    override fun bindToView() {
        // Nothing to do here
    }

    override fun initializeFirebase() {
        Firebase.initialize()
    }

    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperIos(
            firebaseAuth = firebaseAuth,
            serverClientId = configuration.serverClientId,
        )

    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperIos(firebaseAuth = firebaseAuth)
}
