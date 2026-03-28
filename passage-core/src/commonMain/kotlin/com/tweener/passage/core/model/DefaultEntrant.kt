package com.tweener.passage.core.model

/**
 * Default implementation of [Entrant].
 *
 * Provides a simple data class implementation that can be used by any authentication backend.
 */
data class DefaultEntrant(
    override val id: String,
    override val email: String? = null,
    override val displayName: String? = null,
    override val phoneNumber: String? = null,
    override val photoUrl: String? = null,
    override val isAnonymous: Boolean = false,
    override val isEmailVerified: Boolean = false,
    override val metadata: Map<String, Any?> = emptyMap(),
) : Entrant
