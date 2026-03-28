package com.tweener.passage.auth.custom.mapper

import com.tweener.passage.auth.custom.model.CustomUser
import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant

/**
 * Maps a [CustomUser] to an [Entrant].
 *
 * Demonstrates how to create a mapper for a custom authentication backend.
 */
class CustomUserMapper : PassageUserMapper {

    override fun map(backendUser: Any): Entrant {
        val user = backendUser as CustomUser
        return DefaultEntrant(
            id = user.id,
            email = user.email,
            displayName = user.name,
            photoUrl = user.avatarUrl,
            isAnonymous = false,
            isEmailVerified = user.email != null,
            metadata = user.attributes,
        )
    }
}
