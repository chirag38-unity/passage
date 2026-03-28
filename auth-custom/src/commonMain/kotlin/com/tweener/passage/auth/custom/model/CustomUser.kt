package com.tweener.passage.auth.custom.model

/**
 * Represents an authenticated user from a custom HTTP backend.
 *
 * @param id The unique user ID.
 * @param email The user's email address.
 * @param name The user's display name.
 * @param avatarUrl The URL to the user's avatar image.
 * @param token The session/access token from the backend.
 * @param attributes Additional user attributes from the backend.
 */
data class CustomUser(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val token: String? = null,
    val attributes: Map<String, Any?> = emptyMap(),
)
