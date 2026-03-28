package com.tweener.passage.core.model

/**
 * Represents the result of an authentication operation from a backend plugin.
 *
 * Contains the backend-specific user object and an optional ID token.
 * The [backendUser] is later mapped to an [Entrant] via [com.tweener.passage.core.mapper.PassageUserMapper].
 *
 * @param backendUser The raw user object from the authentication backend.
 * @param idToken An optional ID token returned by the backend.
 */
data class PassageAuthResult(
    val backendUser: Any,
    val idToken: String? = null,
)
