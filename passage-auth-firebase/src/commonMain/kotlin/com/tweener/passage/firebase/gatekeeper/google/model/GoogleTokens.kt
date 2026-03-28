package com.tweener.passage.firebase.gatekeeper.google.model

data class GoogleTokens(
    val idToken: String,
    val accessToken: String? = null,
)
