package com.tweener.passage.auth.firebase.error

/**
 * Thrown when no authenticated user is found where one was expected.
 */
class PassageGatekeeperUnknownEntrantException :
    IllegalStateException("No authenticated user was found or created after sign-in.")
