package com.tweener.passage.auth.firebase.model

/**
 * Holds Google authentication tokens extracted from native sign-in flows.
 *
 * @param idToken The Google ID token.
 * @param accessToken An optional Google access token.
 */
data class GoogleTokens(
    val idToken: String,
    val accessToken: String? = null,
)
