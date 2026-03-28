package com.tweener.passage.core

import androidx.compose.runtime.Composable

/**
 * Platform-specific lifecycle binder for Passage.
 *
 * On Android, captures the activity context and activity result launcher needed
 * for native OAuth flows (Google Sign-In, etc.).
 * On iOS, this is a no-op.
 */
expect class PassageViewBinder() {

    /**
     * Binds Passage to the current Composable view.
     * Must be called from a @Composable context.
     */
    @Composable
    fun bind()
}
