package com.tweener.passage.supabase

import com.tweener.passage.core.Passage
import com.tweener.passage.core.model.Entrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Supabase-specific implementation of [Passage].
 *
 * This is a placeholder implementation for future Supabase authentication support.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class PassageSupabase : Passage() {

    override fun getCurrentUser(): Entrant? = null

    override fun getCurrentUserAsFlow(): Flow<Entrant?> = flowOf(null)

    override fun isUserLoggedInAsFlow(): Flow<Boolean> = flowOf(false)

    override suspend fun signOut() {
        // TODO: Implement Supabase sign out
    }

    override suspend fun deleteCurrentUser() {
        // TODO: Implement Supabase user deletion
    }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> =
        Result.failure(NotImplementedError("Supabase authentication is not yet implemented"))
}
