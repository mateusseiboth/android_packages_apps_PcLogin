package org.lineageos.pclogin.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginControllerTest {

    private val names = NewUserNames(user = "New user", guest = "Guest")

    private fun user(id: Int, kind: UserKind = UserKind.FULL) =
        LoginUser(id = id, name = "user $id", kind = kind, creationTime = id.toLong(), isUsable = true)

    private val directory = FakeUserDirectory(
        users = mutableListOf(user(0, UserKind.SYSTEM), user(10), user(11)),
    )
    private val states = mutableListOf<LoginScreen>()
    private val controller = LoginController(directory, names) { states += it }

    @Test
    fun startShowsTheUsersAndWhatCanBeAdded() {
        controller.start()

        val screen = states.last()
        assertEquals(listOf(10, 11), screen.users.map { it.id })
        assertEquals(PodSize.LARGE, screen.podSize)
        assertEquals(Availability.SHOWN, screen.newUser)
        assertEquals(Availability.SHOWN, screen.guest)
        assertNull(screen.busy)
        assertNull(screen.message)
    }

    @Test
    fun startRemovesGuestsLeftBehind() {
        directory.users += user(12, UserKind.GUEST)

        controller.start()

        assertEquals(listOf(12), directory.removed)
    }

    @Test
    fun choosingAUserSwitchesToItAndShowsProgress() {
        controller.start()

        controller.choose(11)

        assertEquals(listOf(11), directory.switches)
        assertEquals(Busy.SigningIn(11), states.last().busy)
    }

    @Test
    fun aSecondChoiceWhileSigningInIsIgnored() {
        controller.start()
        controller.choose(10)

        controller.choose(11)
        controller.addUser()
        controller.startGuest()

        assertEquals(listOf(10), directory.switches)
        assertEquals(3, directory.users.size)
    }

    @Test
    fun aRefusedSwitchReturnsToTheChoiceWithAMessage() {
        directory.isSwitchRefused = true
        controller.start()

        controller.choose(10)

        assertNull(states.last().busy)
        assertEquals(LoginMessage.SIGN_IN_FAILED, states.last().message)
    }

    @Test
    fun startClearsTheProgressWhenTheScreenComesBack() {
        controller.start()
        controller.choose(10)

        controller.start()

        assertNull(states.last().busy)
    }

    @Test
    fun refreshKeepsTheProgressOfAnOngoingSignIn() {
        controller.start()
        controller.choose(10)

        controller.refresh()

        assertEquals(Busy.SigningIn(10), states.last().busy)
    }

    @Test
    fun addUserCreatesAUserWithTheDefaultNameAndSwitchesToIt() {
        controller.start()

        controller.addUser()

        val created = directory.users.last()
        assertEquals("New user", created.name)
        assertEquals(UserKind.FULL, created.kind)
        assertEquals(listOf(created.id), directory.switches)
        assertTrue(states.any { it.busy == Busy.Creating })
    }

    @Test
    fun startGuestCreatesAGuestAndSwitchesToIt() {
        controller.start()

        controller.startGuest()

        val created = directory.users.last()
        assertEquals("Guest", created.name)
        assertEquals(UserKind.GUEST, created.kind)
        assertEquals(listOf(created.id), directory.switches)
    }

    @Test
    fun aFailedCreationReturnsToTheChoiceWithAMessage() {
        directory.isCreationRefused = true
        controller.start()

        controller.addUser()

        assertEquals(LoginMessage.CREATE_FAILED, states.last().message)
        assertNull(states.last().busy)
        assertTrue(directory.switches.isEmpty())
    }

    @Test
    fun nothingIsAddedWhenTheButtonIsNotOffered() {
        directory.additionContext = directory.additionContext.copy(canAddFullUser = false, guestEnabled = false)
        controller.start()

        controller.addUser()
        controller.startGuest()

        assertEquals(3, directory.users.size)
        assertTrue(directory.switches.isEmpty())
    }

    @Test
    fun aNewChoiceClearsAnOldMessage() {
        directory.isSwitchRefused = true
        controller.start()
        controller.choose(10)
        directory.isSwitchRefused = false

        controller.choose(11)

        assertNull(states.last().message)
    }
}
