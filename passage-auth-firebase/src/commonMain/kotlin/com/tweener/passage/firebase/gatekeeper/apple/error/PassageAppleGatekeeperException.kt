package com.tweener.passage.firebase.gatekeeper.apple.error

class PassageAppleGatekeeperException(message: String? = null) : Throwable("An error occurred during Apple Sign In process! ${message?.let { "\n$it" }}")
