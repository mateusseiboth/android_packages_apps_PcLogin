package org.lineageos.pclogin.core

/** How a button that adds someone appears. */
enum class Availability { SHOWN, DISABLED, HIDDEN }

/** The device state that decides whether users and guests can be added from the login screen. */
data class AdditionContext(
    val multipleUsersSupported: Boolean,
    val addUserRestricted: Boolean,
    val guestEnabled: Boolean,
    val canAddFullUser: Boolean,
    val canAddGuest: Boolean,
)

/** Rules for the "Add user" and "Use as guest" buttons: the first rule that matches wins. */
object AdditionRules {

    private class Rule(val matches: (AdditionContext) -> Boolean, val availability: Availability)

    private val hiddenWhenAddingIsOff = Rule(
        { !it.multipleUsersSupported || it.addUserRestricted },
        Availability.HIDDEN,
    )

    private val NEW_USER_RULES = listOf(
        hiddenWhenAddingIsOff,
        Rule({ !it.canAddFullUser }, Availability.DISABLED),
    )

    private val GUEST_RULES = listOf(
        hiddenWhenAddingIsOff,
        Rule({ !it.guestEnabled }, Availability.HIDDEN),
        Rule({ !it.canAddGuest }, Availability.DISABLED),
    )

    fun forNewUser(context: AdditionContext): Availability = apply(NEW_USER_RULES, context)

    fun forGuest(context: AdditionContext): Availability = apply(GUEST_RULES, context)

    private fun apply(rules: List<Rule>, context: AdditionContext): Availability =
        rules.firstOrNull { it.matches(context) }?.availability ?: Availability.SHOWN
}
