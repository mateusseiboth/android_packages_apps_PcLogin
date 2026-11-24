package org.lineageos.pclogin.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.pclogin.R
import org.lineageos.pclogin.core.Availability
import org.lineageos.pclogin.core.LoginScreen

private val BUTTON_PADDING = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

/** Bottom bar: accessibility, then "Use as guest" and "Add user" when the device offers them. */
@Composable
fun LoginBar(screen: LoginScreen, actions: LoginActions, modifier: Modifier = Modifier) {
    val isIdle = screen.busy == null
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val accessibility = stringResource(R.string.accessibility)
        FilledTonalIconButton(onClick = actions.onAccessibility, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Outlined.AccessibilityNew, contentDescription = accessibility)
        }
        BarButton(screen.guest, isIdle, Icons.Outlined.PersonOutline, R.string.use_as_guest, actions.onStartGuest)
        BarButton(screen.newUser, isIdle, Icons.Outlined.PersonAdd, R.string.add_user, actions.onAddUser)
    }
}

@Composable
private fun BarButton(
    availability: Availability,
    isIdle: Boolean,
    icon: ImageVector,
    @StringRes label: Int,
    onClick: () -> Unit,
) {
    if (availability == Availability.HIDDEN) return
    FilledTonalButton(
        onClick = onClick,
        enabled = isIdle && availability == Availability.SHOWN,
        contentPadding = BUTTON_PADDING,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
    }
}
