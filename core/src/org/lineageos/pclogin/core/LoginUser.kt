package org.lineageos.pclogin.core

/** What a user is, as far as the login screen cares. */
enum class UserKind { SYSTEM, FULL, GUEST, PROFILE }

/**
 * A user of the device.
 *
 * [isUsable] is false while the user is being created or removed, or when it is disabled.
 */
data class LoginUser(
    val id: Int,
    val name: String?,
    val kind: UserKind,
    val creationTime: Long,
    val isUsable: Boolean,
)
