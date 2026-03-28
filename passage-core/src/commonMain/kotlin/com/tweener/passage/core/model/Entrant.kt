package com.tweener.passage.core.model

/**
 * Represents a successfully authenticated user.
 *
 * This interface defines the contract for an authenticated user (Entrant) in the Passage library.
 * Implement this interface to provide custom entrant models with additional fields.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
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
