package com.tweener.passage.core.model

/**
 * Represents a successfully authenticated user.
 *
 * This is the core abstraction for an authenticated user in the Passage modular system.
 * Backend-specific user objects are mapped to this interface via [com.tweener.passage.core.mapper.PassageUserMapper].
 *
 * @author Vivien Mahe
 * @since 2024
 */
interface Entrant {
    val id: String
    val email: String?
    val displayName: String?
    val phoneNumber: String?
    val photoUrl: String?
    val isAnonymous: Boolean
    val isEmailVerified: Boolean
    val metadata: Map<String, Any?>
}
