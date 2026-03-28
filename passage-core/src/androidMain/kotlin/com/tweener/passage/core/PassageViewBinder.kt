package com.tweener.passage.core

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of [PassageViewBinder].
 *
 * Captures the activity context and registers an activity result launcher
 * for native OAuth flows that require an Activity (e.g., Google Sign-In).
 */
actual class PassageViewBinder {

    private var _activityContext: Context? = null
    private var _activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    private var _activityResult: ActivityResult? = null

    /**
     * Returns the current Activity context, or null if [bind] has not been called.
     */
    fun getActivityContext(): Context? = _activityContext

    /**
     * Returns the registered activity result launcher, or null if [bind] has not been called.
     */
    fun getLauncher(): ManagedActivityResultLauncher<Intent, ActivityResult>? = _activityResultLauncher

    /**
     * Returns the last activity result, or null if no result has been received.
     */
    fun getActivityResult(): ActivityResult? = _activityResult

    /**
     * Consumes and clears the current activity result to prevent stale results
     * from being used in subsequent authentication flows.
     */
    fun consumeActivityResult() {
        _activityResult = null
    }

    @Composable
    actual fun bind() {
        _activityContext = LocalContext.current

        _activityResultLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            _activityResult = result
        }
    }
}
