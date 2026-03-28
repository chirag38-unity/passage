package com.tweener.passage.core

import androidx.compose.runtime.Composable

/**
 * iOS implementation of [PassageViewBinder].
 *
 * On iOS, no special view binding is required for authentication flows.
 */
actual class PassageViewBinder {

    @Composable
    actual fun bind() {
        // No-op on iOS
    }
}
