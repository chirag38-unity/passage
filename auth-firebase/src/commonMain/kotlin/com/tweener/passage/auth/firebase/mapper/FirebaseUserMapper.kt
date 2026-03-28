package com.tweener.passage.auth.firebase.mapper

import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Maps a [FirebaseUser] to an [Entrant].
 *
 * Extracts user profile information from the Firebase user object and its provider data,
 * falling back to provider data for display name, phone number, and photo URL.
 */
class FirebaseUserMapper : PassageUserMapper {

    override fun map(backendUser: Any): Entrant {
        val user = backendUser as FirebaseUser
        return DefaultEntrant(
            id = user.uid,
            email = user.email,
            displayName = user.displayName ?: user.providerData.mapNotNull { it.displayName }.firstOrNull(),
            phoneNumber = user.phoneNumber ?: user.providerData.mapNotNull { it.phoneNumber }.firstOrNull(),
            photoUrl = user.photoURL ?: user.providerData.mapNotNull { it.photoURL }.firstOrNull(),
            isAnonymous = user.isAnonymous,
            isEmailVerified = user.isEmailVerified,
        )
    }
}
