package com.tweener.passage.auth.supabase.handler

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.AuthCredentialHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken

/**
 * Handles authentication credentials using the Supabase Auth SDK.
 *
 * Supports email/password and ID token-based (Google, Apple) authentication.
 *
 * @param client The Supabase client instance.
 */
class SupabaseCredentialHandler(
    private val client: SupabaseClient,
) : AuthCredentialHandler {

    override fun canHandle(credential: AuthCredential): Boolean =
        credential is AuthCredential.EmailPassword || credential is AuthCredential.IdToken

    override suspend fun authenticate(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        when (credential) {
            is AuthCredential.EmailPassword -> {
                client.auth.signInWith(Email) {
                    email = credential.email
                    password = credential.password
                }
            }

            is AuthCredential.IdToken -> {
                client.auth.signInWith(IDToken) {
                    idToken = credential.idToken
                    provider = when (credential.provider) {
                        "apple" -> Apple
                        else -> Google
                    }
                    credential.nonce?.let { nonce = it }
                    credential.accessToken?.let { accessToken = it }
                }
            }

            else -> throw IllegalArgumentException("Unsupported credential type for Supabase: ${credential::class.simpleName}")
        }

        val user = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Supabase sign-in succeeded but no user was returned.")
        PassageAuthResult(backendUser = user)
    }

    override suspend fun createUser(credential: AuthCredential): Result<PassageAuthResult> = runCatching {
        val params = credential as? AuthCredential.EmailPassword
            ?: throw IllegalArgumentException("Supabase sign-up requires EmailPassword credentials.")

        client.auth.signUpWith(Email) {
            email = params.email
            password = params.password
        }

        val user = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Supabase sign-up succeeded but no user was returned.")
        PassageAuthResult(backendUser = user)
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        authenticate(credential).map { }
}
