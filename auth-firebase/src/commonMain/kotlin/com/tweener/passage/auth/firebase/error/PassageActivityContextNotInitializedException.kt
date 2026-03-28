package com.tweener.passage.auth.firebase.error

/**
 * Thrown when the activity context has not been initialized via `PassageCore.bindToView()`.
 */
class PassageActivityContextNotInitializedException :
    IllegalStateException("Activity context is not initialized. Call PassageCore.bindToView() from a @Composable context first.")
