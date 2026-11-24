package org.lineageos.pclogin

import android.app.ActivityManager
import android.content.Context
import android.content.pm.UserInfo
import android.os.NewUserRequest
import android.os.UserHandle
import android.os.UserManager
import org.lineageos.pclogin.core.AdditionContext
import org.lineageos.pclogin.core.LoginUser
import org.lineageos.pclogin.core.UserDirectory
import org.lineageos.pclogin.core.UserKind

/** [UserDirectory] over UserManager and ActivityManager. Runs as the headless system user. */
class AndroidUserDirectory(private val context: Context) : UserDirectory {

    private val userManager = context.getSystemService(UserManager::class.java)
    private val activityManager = context.getSystemService(ActivityManager::class.java)

    override val currentUserId: Int
        get() = ActivityManager.getCurrentUser()

    override fun listUsers(): List<LoginUser> = userManager.aliveUsers.map(::toLoginUser)

    override fun readAdditionContext(): AdditionContext = AdditionContext(
        multipleUsersSupported = UserManager.supportsMultipleUsers(),
        addUserRestricted = isRestricted(UserManager.DISALLOW_ADD_USER),
        guestEnabled = context.resources.getBoolean(R.bool.config_showGuestButton) &&
            !isRestricted(UserManager.DISALLOW_ADD_GUEST),
        canAddFullUser = userManager.canAddMoreUsers(UserManager.USER_TYPE_FULL_SECONDARY),
        canAddGuest = userManager.canAddMoreUsers(UserManager.USER_TYPE_FULL_GUEST),
    )

    override fun switchTo(userId: Int): Boolean = activityManager.switchUser(UserHandle.of(userId))

    override fun createUser(name: String): Int? {
        val request = NewUserRequest.Builder()
            .setName(name)
            .setUserType(UserManager.USER_TYPE_FULL_SECONDARY)
            .build()
        return userManager.createUser(request).user?.identifier
    }

    override fun createGuest(name: String): Int? =
        userManager.createGuest(context)?.id?.also { userManager.setUserName(it, name) }

    override fun removeUser(userId: Int) {
        userManager.removeUser(UserHandle.of(userId))
    }

    /**
     * A restriction blocks the login screen when it is set on the headless system user (it creates
     * the users) or when an admin policy sets it on any user, so no one adds users from here that
     * a managed user may not add.
     */
    private fun isRestricted(restriction: String): Boolean =
        userManager.hasUserRestrictionForUser(restriction, UserHandle.SYSTEM) ||
            userManager.aliveUsers.any { isSetByPolicy(restriction, it.userHandle) }

    private fun isSetByPolicy(restriction: String, user: UserHandle): Boolean =
        userManager.getUserRestrictionSources(restriction, user)
            .any { it.userRestrictionSource in POLICY_SOURCES }

    private fun toLoginUser(info: UserInfo) = LoginUser(
        id = info.id,
        name = info.name,
        kind = KIND_RULES.firstOrNull { (matches, _) -> matches(info) }?.second ?: UserKind.SYSTEM,
        creationTime = info.creationTime,
        isUsable = info.isEnabled && !info.partial && !info.preCreated,
    )

    private companion object {
        /** First match wins: a guest is also a full user. */
        val KIND_RULES: List<Pair<(UserInfo) -> Boolean, UserKind>> = listOf(
            UserInfo::isGuest to UserKind.GUEST,
            UserInfo::isProfile to UserKind.PROFILE,
            UserInfo::isFull to UserKind.FULL,
        )

        val POLICY_SOURCES = setOf(
            UserManager.RESTRICTION_SOURCE_DEVICE_OWNER,
            UserManager.RESTRICTION_SOURCE_PROFILE_OWNER,
        )
    }
}
