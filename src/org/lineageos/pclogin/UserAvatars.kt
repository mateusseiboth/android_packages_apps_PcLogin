package org.lineageos.pclogin

import android.content.Context
import android.graphics.Bitmap
import android.os.UserManager
import com.android.internal.util.UserIcons

/** A user's picture, or the system's default picture for that user. Blocks; call off the main thread. */
class UserAvatars(private val context: Context) {

    private val userManager = context.getSystemService(UserManager::class.java)

    fun load(userId: Int): Bitmap =
        userManager.getUserIcon(userId) ?: UserIcons.convertToBitmap(
            UserIcons.getDefaultUserIcon(context.resources, userId, /* light= */ false)
        )
}
