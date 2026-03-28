package com.tweener.passage.auth.custom.model

/**
 * Configuration for the custom HTTP authentication backend.
 *
 * @param baseUrl The base URL of the authentication API (e.g., "https://api.example.com").
 */
data class CustomAuthConfig(
    val baseUrl: String,
)
