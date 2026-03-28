package com.tweener.passage.auth.supabase.mapper

import com.tweener.passage.auth.supabase.model.SupabaseUser
import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant

/**
 * Maps a [SupabaseUser] to an [Entrant].
 *
 * Extracts user profile information from the Supabase user object,
 * using user metadata for fields like display name and photo URL.
 */
class SupabaseUserMapper : PassageUserMapper {

    override fun map(backendUser: Any): Entrant {
        val user = backendUser as SupabaseUser
        return DefaultEntrant(
            id = user.id,
            email = user.email,
            displayName = user.userMetadata["full_name"] as? String,
            phoneNumber = user.phone,
            photoUrl = user.userMetadata["avatar_url"] as? String,
            isAnonymous = false,
            isEmailVerified = user.isEmailVerified,
            metadata = user.userMetadata,
        )
    }
}
