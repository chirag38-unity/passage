package com.tweener.passage.firebase.gatekeeper.email.model

data class PassageResetPasswordParams(
    val oobCode: String,
    val newPassword: String,
)
