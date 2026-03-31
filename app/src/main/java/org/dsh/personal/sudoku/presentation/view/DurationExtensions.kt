package org.dsh.personal.sudoku.presentation.view

import kotlin.time.Duration

/**
 * Formats a Duration to a user-friendly string format.
 * Examples:
 * - 5 minutes 32 seconds -> "5:32"
 * - 1 hour 23 minutes 45 seconds -> "1:23:45"
 * - 45 seconds -> "0:45"
 */
fun Duration.toFormattedString(): String {
    val totalSeconds = this.inWholeSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}
