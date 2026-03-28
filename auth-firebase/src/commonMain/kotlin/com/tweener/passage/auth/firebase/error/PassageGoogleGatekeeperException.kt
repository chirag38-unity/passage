package com.tweener.passage.auth.firebase.error

/**
 * Thrown when Google Sign-In encounters an error.
 */
class PassageGoogleGatekeeperException(message: String? = "An error occurred during Google Sign-In.") :
    Exception(message)
