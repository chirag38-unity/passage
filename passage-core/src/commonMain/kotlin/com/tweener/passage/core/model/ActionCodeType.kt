package com.tweener.passage.core.model

/**
 * Represents the type of an out-of-band (OOB) action code used in email-based authentication flows.
 *
 * Each type corresponds to a specific email action handled by the [AuthPlugin].
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
sealed interface ActionCodeType {

    /** Email address verification action. */
    data object VerifyEmail : ActionCodeType

    /** Password reset action. */
    data object PasswordReset : ActionCodeType

    /** Sign-in via email link (magic link) action. */
    data object SignInWithEmailLink : ActionCodeType
}