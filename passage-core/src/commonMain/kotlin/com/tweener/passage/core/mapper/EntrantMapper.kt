package com.tweener.passage.core.mapper

import com.tweener.passage.core.model.Entrant

/**
 * A functional interface for mapping backend-specific user representations to [Entrant].
 *
 * Implement this interface to provide custom mapping logic for different authentication backends.
 *
 * @param T The backend-specific user type to map from.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
fun interface EntrantMapper<T> {
    fun map(user: T): Entrant
}
