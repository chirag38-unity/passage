package com.tweener.passage.auth.supabase

import com.tweener.passage.auth.supabase.handler.SupabaseCredentialHandler
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.PassageAuthPlugin
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Supabase implementation of [PassageAuthPlugin].
 *
 * Provides authentication using the real Supabase SDK via an injected [SupabaseClient].
 * The client instance must be created and configured externally — this plugin does NOT
 * create the client internally, following the same pattern as [com.tweener.passage.auth.firebase.FirebaseAuthPlugin].
 *
 * ### Usage
 * ```kotlin
 * val supabaseClient = createSupabaseClient(
 *     supabaseUrl = "https://your-project.supabase.co",
 *     supabaseKey = "your-anon-key"
 * ) {
 *     install(Auth)
 * }
 *
 * val plugin = SupabaseAuthPlugin(client = supabaseClient)
 * val core = PassageCore(
 *     authPlugin = plugin,
 *     userMapper = SupabaseUserMapper()
 * )
 * ```
 *
 * @param client The externally-created Supabase client instance.
 */
class SupabaseAuthPlugin(
    private val client: SupabaseClient,
) : PassageAuthPlugin {

    private val credentialHandler = SupabaseCredentialHandler(client)

    override suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult> =
        credentialHandler.authenticate(credential)

    override suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult> =
        credentialHandler.createUser(credential)

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        signIn(credential).map { }

    override fun getCurrentUser(): Any? =
        client.auth.currentUserOrNull()

    override fun observeAuthState(): Flow<Any?> =
        client.auth.sessionStatus.map {
            when (it) {
                is SessionStatus.Authenticated -> client.auth.currentUserOrNull()
                else -> null
            }
        }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = runCatching {
        if (forceRefresh) {
            client.auth.refreshCurrentSession()
        }
        client.auth.currentSessionOrNull()?.accessToken
            ?: error("No active Supabase session found.")
    }

    override suspend fun deleteCurrentUser() {
        throw UnsupportedOperationException("Deleting users requires server-side admin privileges in Supabase.")
    }
}
