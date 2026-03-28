package com.tweener.passage.auth.supabase.model

/**
 * Represents an authenticated user from Supabase.
 *
 * @param id The unique user ID from Supabase.
 * @param email The user's email address.
 * @param phone The user's phone number.
 * @param userMetadata Additional metadata from the Supabase user profile.
 * @param isEmailVerified Whether the user's email has been verified.
 */
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val userMetadata: Map<String, Any?> = emptyMap(),
    val isEmailVerified: Boolean = false,
)
