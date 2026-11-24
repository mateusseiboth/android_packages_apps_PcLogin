package org.lineageos.pclogin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.lineageos.pclogin.R
import org.lineageos.pclogin.UserAvatars
import org.lineageos.pclogin.core.LoginMessage
import org.lineageos.pclogin.core.LoginScreen

/** What the screen's buttons do. */
class LoginActions(
    val onChoose: (Int) -> Unit,
    val onAddUser: () -> Unit,
    val onStartGuest: () -> Unit,
    val onAccessibility: () -> Unit,
)

private val MESSAGES = mapOf(
    LoginMessage.SIGN_IN_FAILED to R.string.sign_in_failed,
    LoginMessage.CREATE_FAILED to R.string.create_failed,
)

/** Scrim over the wallpaper: lighter when the wallpaper is already blurred. */
private val SCRIM_ALPHA = mapOf(true to 0.24f, false to 0.6f)

@Composable
fun LoginContent(
    screenState: StateFlow<LoginScreen>,
    avatars: UserAvatars,
    isBlurred: Boolean,
    actions: LoginActions,
) {
    val screen by screenState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA.getValue(isBlurred))),
    ) {
        UserPods(screen, avatars, actions, Modifier.align(Alignment.Center))
        LoginBar(
            screen = screen,
            actions = actions,
            modifier = Modifier.align(Alignment.BottomStart).safeDrawingPadding().padding(16.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserPods(screen: LoginScreen, avatars: UserAvatars, actions: LoginActions, modifier: Modifier) {
    Column(modifier = modifier.padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            screen.users.forEach { user ->
                UserPod(
                    user = user,
                    size = screen.podSize,
                    busy = screen.busy,
                    avatars = avatars,
                    onClick = { actions.onChoose(user.id) },
                )
            }
        }
        screen.message?.let { message ->
            Text(
                text = stringResource(MESSAGES.getValue(message)),
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
