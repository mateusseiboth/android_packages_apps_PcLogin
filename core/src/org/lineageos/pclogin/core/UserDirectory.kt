package org.lineageos.pclogin.core

/** The device's users, as the login screen reads and changes them. Calls may block. */
interface UserDirectory {
    val currentUserId: Int

    fun listUsers(): List<LoginUser>

    fun readAdditionContext(): AdditionContext

    /** Brings [userId] to the foreground; false when the system refuses. */
    fun switchTo(userId: Int): Boolean

    /** Creates a full user; its id, or null when the system refuses. */
    fun createUser(name: String): Int?

    /** Creates a guest; its id, or null when the system refuses. */
    fun createGuest(name: String): Int?

    fun removeUser(userId: Int)
}

/** Names given to users created from the login screen, in the device's language. */
data class NewUserNames(val user: String, val guest: String)
