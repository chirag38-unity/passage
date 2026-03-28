package com.tweener.passage.firebase

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.tweener.passage.firebase.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.firebase.gatekeeper.apple.PassageAppleGatekeeperAndroid
import com.tweener.passage.firebase.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.firebase.gatekeeper.google.PassageGoogleGatekeeperAndroid
import com.tweener.passage.firebase.model.AppleGatekeeperConfiguration
import com.tweener.passage.firebase.model.GoogleGatekeeperConfiguration
import dev.gitlive.firebase.auth.FirebaseAuth

class PassageFirebaseAndroid(private val applicationContext: Context) : PassageFirebase() {

    private var activityContext: Context? = null
    private var activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    private var activityResult: ActivityResult? = null

    @Composable
    override fun bindToView() {
        activityContext = LocalContext.current

        activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            activityResult = result
        }
    }

    override fun initializeFirebase() {
        // Nothing to do here
    }

    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperAndroid(
            serverClientId = configuration.serverClientId,
            firebaseAuth = firebaseAuth,
            applicationContext = applicationContext,
            activityContext = { activityContext },
            activityResultLauncher = { activityResultLauncher },
            activityResult = { activityResult },
            useGoogleButtonFlow = configuration.android.useGoogleButtonFlow,
            filterByAuthorizedAccounts = configuration.android.filterByAuthorizedAccounts,
            autoSelectEnabled = configuration.android.autoSelectEnabled,
            maxRetries = configuration.android.maxRetries,
        )

    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperAndroid()
}
