package org.lineageos.pclogin.core

/** Which users the login screen shows, and which leftovers it cleans up. */
object LoginRoster {

    private val ORDER = compareBy<LoginUser>({ it.creationTime }, { it.id })

    /** The people who can sign in: usable full users, oldest first. Guests start from their own button. */
    fun of(users: List<LoginUser>): List<LoginUser> =
        users.filter { it.kind == UserKind.FULL && it.isUsable }.sortedWith(ORDER)

    /** Guests of earlier sessions: every guest except the one running in the foreground. */
    fun staleGuests(users: List<LoginUser>, currentUserId: Int): List<Int> =
        users.filter { it.kind == UserKind.GUEST && it.id != currentUserId }.map { it.id }
}
