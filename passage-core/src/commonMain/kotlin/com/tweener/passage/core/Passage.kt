package com.tweener.passage.core

import com.tweener.passage.core.model.Entrant
import com.tweener.passage.core.model.PassageUniversalLink
import com.tweener.passage.core.universallink.PassageUniversalLinkHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The authentication entry point—a secure gateway through which users (Entrants)
 * must pass to access the system.
 *
 * The **Passage** represents the authentication entry point. To navigate the Passage, Entrants are verified by Gatekeepers,
 * which act as authentication providers (e.g., Google, Apple, or Email/Password).
 *
 * ### Key concepts
 * - **Passage**: The entry point for authentication. It facilitates interactions between Entrants and Gatekeepers.
 * - **Gatekeeper**: An authentication provider that verifies an Entrant's identity (e.g., Google, Apple, Email/Password).
 * - **Entrant**: A user who has been successfully authenticated and granted access to the system.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
abstract class Passage {
    private val universalLinkHandler = PassageUniversalLinkHandler()

    val universalLinkToHandle: StateFlow<PassageUniversalLink?> = universalLinkHandler.linkToHandle

    /**
     * Retrieves the currently authenticated user as an [Entrant], or `null` if no user is authenticated.
     *
     * @return The current [Entrant], or `null` if no user is logged in.
     */
    abstract fun getCurrentUser(): Entrant?

    /**
     * Observes changes to the authenticated user as a [Flow] of [Entrant].
     *
     * @return A [Flow] that emits the current [Entrant], or `null` if no user is logged in.
     */
    abstract fun getCurrentUserAsFlow(): Flow<Entrant?>

    /**
     * Indicates whether a user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    /**
     * Observes whether a user is logged in as a [Flow] of [Boolean].
     *
     * @return A [Flow] that emits `true` if a user is logged in, or `false` otherwise.
     */
    abstract fun isUserLoggedInAsFlow(): Flow<Boolean>

    /**
     * Signs out the current user from all configured Gatekeepers.
     */
    abstract suspend fun signOut()

    /**
     * Deletes the currently authenticated user.
     */
    abstract suspend fun deleteCurrentUser()

    /**
     * Retrieves the ID token for the currently authenticated user.
     *
     * @param forceRefresh If `true`, forces a refresh of the ID token. Defaults to `false`.
     * @return A [Result] containing the ID token as a [String], or an error if no user is authenticated.
     */
    abstract suspend fun getIdToken(forceRefresh: Boolean = false): Result<String>

    // region Universal Links

    /**
     * Handles a Universal Link by passing the provided URL to the link handler.
     *
     * Call this method whenever your app receives a universal link (iOS) or App Link (Android) to allow Passage to process it.
     * This is typically used to handle deep linking for authentication flows, such as email verification or password reset links.
     *
     * @param url The URL from the universal or App Link.
     */
    fun handleLink(url: String): Boolean =
        universalLinkHandler.handle(url = url)

    /**
     * Notifies Passage that a universal link has been handled.
     *
     * Call this method whenever a universal link or App Link is successfully handled in your app.
     * This ensures that Passage processes the link appropriately, allowing it to update the
     * authentication state or perform other related tasks.
     */
    fun onLinkHandled() {
        universalLinkHandler.onLinkHandled()
    }

    // endregion Universal Links
}
