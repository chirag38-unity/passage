package com.tweener.passage.sample

import com.tweener.passage.firebase.PassageFirebase

/**
 * @author Vivien Mahe
 * @since 03/12/2024
 */

class PassageHelper {

    private val passage: PassageFirebase = providePassage()

    fun handle(url: String): Boolean =
        passage.handleLink(url = url)

}
