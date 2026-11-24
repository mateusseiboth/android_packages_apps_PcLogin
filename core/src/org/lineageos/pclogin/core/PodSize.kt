package org.lineageos.pclogin.core

/** Size of the user pods: large while a few users fit in one row, small after that. */
enum class PodSize(val maxUsers: Int) {
    LARGE(maxUsers = 4),
    SMALL(maxUsers = Int.MAX_VALUE);

    companion object {
        fun forCount(count: Int): PodSize = entries.first { count <= it.maxUsers }
    }
}
