package com.tweener.passage.core.model

/**
 * Default implementation of [Entrant] providing a standard authenticated user representation.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
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
