package com.tweener.passage.firebase.gatekeeper.google.error

class PassageActivityContextNotInitializedException : Throwable("You must call Passage.bindToView() in your @Composable before performing any authentication operation.")
