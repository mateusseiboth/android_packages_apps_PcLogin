package org.lineageos.pclogin.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AdditionRulesTest {

    private val open = AdditionContext(
        multipleUsersSupported = true,
        addUserRestricted = false,
        guestEnabled = true,
        canAddFullUser = true,
        canAddGuest = true,
    )

    @Test
    fun offersBothWhenNothingStandsInTheWay() {
        assertEquals(Availability.SHOWN, AdditionRules.forNewUser(open))
        assertEquals(Availability.SHOWN, AdditionRules.forGuest(open))
    }

    @Test
    fun hidesBothWithoutMultipleUsers() {
        val context = open.copy(multipleUsersSupported = false)

        assertEquals(Availability.HIDDEN, AdditionRules.forNewUser(context))
        assertEquals(Availability.HIDDEN, AdditionRules.forGuest(context))
    }

    @Test
    fun hidesBothWhenAddingUsersIsRestricted() {
        val context = open.copy(addUserRestricted = true)

        assertEquals(Availability.HIDDEN, AdditionRules.forNewUser(context))
        assertEquals(Availability.HIDDEN, AdditionRules.forGuest(context))
    }

    @Test
    fun hidesOnlyTheGuestWhenGuestsAreTurnedOff() {
        val context = open.copy(guestEnabled = false)

        assertEquals(Availability.SHOWN, AdditionRules.forNewUser(context))
        assertEquals(Availability.HIDDEN, AdditionRules.forGuest(context))
    }

    @Test
    fun disablesEachOneAtItsLimit() {
        assertEquals(Availability.DISABLED, AdditionRules.forNewUser(open.copy(canAddFullUser = false)))
        assertEquals(Availability.SHOWN, AdditionRules.forGuest(open.copy(canAddFullUser = false)))
        assertEquals(Availability.DISABLED, AdditionRules.forGuest(open.copy(canAddGuest = false)))
        assertEquals(Availability.SHOWN, AdditionRules.forNewUser(open.copy(canAddGuest = false)))
    }

    @Test
    fun aRestrictionWinsOverTheLimit() {
        val context = open.copy(addUserRestricted = true, canAddFullUser = false, canAddGuest = false)

        assertEquals(Availability.HIDDEN, AdditionRules.forNewUser(context))
        assertEquals(Availability.HIDDEN, AdditionRules.forGuest(context))
    }
}
