package com.tweener.passage.auth.firebase.error

/**
 * Thrown when a gatekeeper is not implemented for the current platform.
 */
class PassageGatekeeperNotImplementedException(gatekeeper: String, platform: String) :
    UnsupportedOperationException("Passage does not yet handle gatekeeper $gatekeeper on platform $platform")
