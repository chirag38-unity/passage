package com.tweener.passage.firebase.mapper

import com.tweener.passage.core.mapper.EntrantMapper
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Maps a [FirebaseUser] to a [DefaultEntrant].
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class FirebaseEntrantMapper : EntrantMapper<FirebaseUser> {
    override fun map(user: FirebaseUser): Entrant =
        DefaultEntrant(
            id = user.uid,
            email = user.email,
            displayName = user.displayName ?: user.providerData.map { it.displayName }.firstOrNull { it != null },
            phoneNumber = user.phoneNumber ?: user.providerData.map { it.phoneNumber }.firstOrNull { it != null },
            photoUrl = user.photoURL ?: user.providerData.map { it.photoURL }.firstOrNull { it != null },
            isAnonymous = user.isAnonymous,
            isEmailVerified = user.isEmailVerified,
        )
}
