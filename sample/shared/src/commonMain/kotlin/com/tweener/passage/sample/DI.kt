package com.tweener.passage.sample

import com.tweener.passage.firebase.PassageFirebase

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */

private val passage: PassageFirebase = createPassage()

expect fun createPassage(): PassageFirebase

fun providePassage(): PassageFirebase = passage

