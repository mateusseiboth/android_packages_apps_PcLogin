package org.lineageos.pclogin.core

/** A user list in memory, with switches and creations that succeed unless told otherwise. */
class FakeUserDirectory(
    var users: MutableList<LoginUser> = mutableListOf(),
    var additionContext: AdditionContext = AdditionContext(
        multipleUsersSupported = true,
        addUserRestricted = false,
        guestEnabled = true,
        canAddFullUser = true,
        canAddGuest = true,
    ),
    override var currentUserId: Int = 0,
) : UserDirectory {
    var isSwitchRefused = false
    var isCreationRefused = false
    val switches = mutableListOf<Int>()
    val removed = mutableListOf<Int>()
    private var nextId = 100

    override fun listUsers(): List<LoginUser> = users.toList()

    override fun readAdditionContext(): AdditionContext = additionContext

    override fun switchTo(userId: Int): Boolean {
        switches += userId
        return !isSwitchRefused
    }

    override fun createUser(name: String): Int? = addUser(name, UserKind.FULL)

    override fun createGuest(name: String): Int? = addUser(name, UserKind.GUEST)

    override fun removeUser(userId: Int) {
        removed += userId
        users.removeAll { it.id == userId }
    }

    private fun addUser(name: String, kind: UserKind): Int? {
        if (isCreationRefused) return null
        val id = nextId++
        users += LoginUser(id = id, name = name, kind = kind, creationTime = id.toLong(), isUsable = true)
        return id
    }
}
