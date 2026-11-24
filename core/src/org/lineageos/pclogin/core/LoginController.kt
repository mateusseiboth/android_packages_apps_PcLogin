package org.lineageos.pclogin.core

/** Something the login screen is waiting for. */
sealed interface Busy {
    data class SigningIn(val userId: Int) : Busy

    data object Creating : Busy
}

/** Why the last action did not go through. */
enum class LoginMessage { SIGN_IN_FAILED, CREATE_FAILED }

/** Everything the login screen draws. */
data class LoginScreen(
    val users: List<LoginUser> = emptyList(),
    val podSize: PodSize = PodSize.LARGE,
    val newUser: Availability = Availability.HIDDEN,
    val guest: Availability = Availability.HIDDEN,
    val busy: Busy? = null,
    val message: LoginMessage? = null,
)

/**
 * The login screen's flow: show the users, sign one in, or add a user or a guest and sign it in.
 *
 * Calls block on [directory]; run them off the main thread, one at a time. Every new screen goes to
 * [publish].
 */
class LoginController(
    private val directory: UserDirectory,
    private val names: NewUserNames,
    private val publish: (LoginScreen) -> Unit,
) {
    private var screen = LoginScreen()

    /** The screen came to the front: clean up old guests and show the users again. */
    fun start() {
        val users = directory.listUsers()
        LoginRoster.staleGuests(users, directory.currentUserId).forEach(directory::removeUser)
        show(read().copy(busy = null))
    }

    /** The users changed: show them again, without stopping a sign in in progress. */
    fun refresh() {
        show(read().copy(busy = screen.busy, message = screen.message))
    }

    fun choose(userId: Int) {
        if (screen.busy != null) return
        signIn(userId)
    }

    fun addUser() {
        createAndSignIn(screen.newUser) { directory.createUser(names.user) }
    }

    fun startGuest() {
        createAndSignIn(screen.guest) { directory.createGuest(names.guest) }
    }

    private fun createAndSignIn(availability: Availability, create: () -> Int?) {
        if (screen.busy != null || availability != Availability.SHOWN) return
        show(screen.copy(busy = Busy.Creating, message = null))
        val userId = create() ?: return fail(LoginMessage.CREATE_FAILED)
        signIn(userId)
    }

    private fun signIn(userId: Int) {
        show(screen.copy(busy = Busy.SigningIn(userId), message = null))
        if (!directory.switchTo(userId)) fail(LoginMessage.SIGN_IN_FAILED)
    }

    private fun fail(message: LoginMessage) {
        show(read().copy(message = message))
    }

    private fun read(): LoginScreen {
        val users = LoginRoster.of(directory.listUsers())
        val context = directory.readAdditionContext()
        return LoginScreen(
            users = users,
            podSize = PodSize.forCount(users.size),
            newUser = AdditionRules.forNewUser(context),
            guest = AdditionRules.forGuest(context),
        )
    }

    private fun show(next: LoginScreen) {
        screen = next
        publish(next)
    }
}
