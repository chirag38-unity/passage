package com.tweener.passage.core.model

/**
 * Represents credentials used to authenticate a user.
 *
 * Each subtype corresponds to a specific authentication method.
 * Plugins and credential handlers use this to determine how to process the authentication request.
 */
sealed interface AuthCredential {

    /**
     * Email and password credentials.
     */
    data class EmailPassword(val email: String, val password: String) : AuthCredential

    /**
     * ID Token-based credentials (e.g., Google Sign-In, Supabase ID Token).
     *
     * @param idToken The identity token from the provider.
     * @param accessToken An optional access token from the provider.
     * @param provider An optional provider identifier (e.g., "google", "apple").
     */
    data class IdToken(
        val idToken: String,
        val accessToken: String? = null,
        val provider: String? = null,
    ) : AuthCredential

    /**
     * Email link (passwordless) credentials.
     */
    data class EmailLink(val email: String, val link: String) : AuthCredential

    /**
     * Custom credentials for extensible authentication flows.
     *
     * Use this for backend-specific credential types that don't fit the standard categories.
     */
    data class Custom(val data: Map<String, String>) : AuthCredential
}
