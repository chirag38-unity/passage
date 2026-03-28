package com.tweener.passage.auth.firebase.error

/**
 * Thrown when an unexpected credential type is received during Google Sign-In.
 */
class PassageGoogleGatekeeperUnknownCredentialException :
    IllegalStateException("Unexpected type of credential received during Google Sign-In.")
