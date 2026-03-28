package com.tweener.passage.auth.firebase.error

/**
 * Thrown when Apple Sign-In encounters an error.
 */
class PassageAppleGatekeeperException(message: String? = "An error occurred during Apple Sign-In.") :
    Exception(message)
