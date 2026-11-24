package org.lineageos.pclogin.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginRosterTest {

    private fun user(id: Int, kind: UserKind = UserKind.FULL, created: Long = id.toLong(), usable: Boolean = true) =
        LoginUser(id = id, name = "user $id", kind = kind, creationTime = created, isUsable = usable)

    @Test
    fun keepsOnlyUsableFullUsers() {
        val users = listOf(
            user(0, UserKind.SYSTEM),
            user(10),
            user(11, UserKind.GUEST),
            user(12, UserKind.PROFILE),
            user(13, usable = false),
            user(14),
        )

        assertEquals(listOf(10, 14), LoginRoster.of(users).map { it.id })
    }

    @Test
    fun sortsByCreationTimeThenId() {
        val users = listOf(user(12, created = 50), user(11, created = 10), user(10, created = 50))

        assertEquals(listOf(11, 10, 12), LoginRoster.of(users).map { it.id })
    }

    @Test
    fun findsGuestsLeftBehindByEarlierSessions() {
        val users = listOf(user(0, UserKind.SYSTEM), user(10), user(11, UserKind.GUEST), user(12, UserKind.GUEST))

        assertEquals(listOf(11, 12), LoginRoster.staleGuests(users, currentUserId = 0))
        assertEquals(listOf(12), LoginRoster.staleGuests(users, currentUserId = 11))
    }
}
