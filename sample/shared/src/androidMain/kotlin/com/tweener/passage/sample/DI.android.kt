package com.tweener.passage.sample

import com.tweener.passage.firebase.PassageFirebase
import com.tweener.passage.firebase.PassageFirebaseAndroid

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */

actual fun createPassage(): PassageFirebase = PassageFirebaseAndroid(applicationContext = applicationContext)
