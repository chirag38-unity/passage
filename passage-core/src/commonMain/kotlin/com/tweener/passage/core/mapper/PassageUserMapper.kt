package com.tweener.passage.core.mapper

import com.tweener.passage.core.model.Entrant

/**
 * Maps a backend-specific user object to an [Entrant].
 *
 * Each authentication backend produces its own user type (e.g., FirebaseUser, SupabaseUser).
 * Implement this interface to convert those backend users into the unified [Entrant] model.
 */
fun interface PassageUserMapper {

    /**
     * Maps a backend-specific user object to an [Entrant].
     *
     * @param backendUser The raw user object from the authentication backend.
     * @return The mapped [Entrant].
     */
    fun map(backendUser: Any): Entrant
}
