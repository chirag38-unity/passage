package com.tweener.passage.auth.supabase.mapper

import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant
import io.github.jan.supabase.auth.user.UserInfo

/**
 * Maps a Supabase [UserInfo] to an [Entrant].
 *
 * Extracts user profile information from the Supabase user object.
 * Uses [UserInfo] from the official Supabase SDK.
 */
class SupabaseUserMapper : PassageUserMapper {

    override fun map(backendUser: Any): Entrant {
        val user = backendUser as UserInfo
        return DefaultEntrant(
            id = user.id,
            email = user.email,
            displayName = user.userMetadata?.get("full_name")?.toString(),
            phoneNumber = user.phone,
            photoUrl = user.userMetadata?.get("avatar_url")?.toString(),
            isAnonymous = false,
            isEmailVerified = user.emailConfirmedAt != null,
        )
    }
}
