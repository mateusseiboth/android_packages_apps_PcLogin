package org.lineageos.pclogin

import android.content.Context
import android.content.Intent
import com.android.internal.accessibility.common.ShortcutChooserDialogConstants
import com.android.internal.accessibility.common.ShortcutConstants.UserShortcutType

/** Opens SystemUI's accessibility quick access dialog on the login screen's display. */
object AccessibilityLauncher {

    fun open(context: Context) {
        val intent = Intent(ShortcutChooserDialogConstants.LAUNCH_SHORTCUT_CHOOSER_DIALOG_ACTION)
            .putExtra(ShortcutChooserDialogConstants.SHORTCUT_TYPE, UserShortcutType.QUICK_ACCESS)
            .putExtra(ShortcutChooserDialogConstants.DISPLAY_ID, context.displayId)
        context.sendBroadcast(intent)
    }
}
