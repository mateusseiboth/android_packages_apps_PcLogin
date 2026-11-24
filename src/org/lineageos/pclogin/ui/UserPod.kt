package org.lineageos.pclogin.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lineageos.pclogin.R
import org.lineageos.pclogin.UserAvatars
import org.lineageos.pclogin.core.Busy
import org.lineageos.pclogin.core.LoginUser
import org.lineageos.pclogin.core.PodSize

/** Measures of a pod: picture, padding around it, gap to the name and the name's style. */
private class PodMetrics(
    val avatar: Dp,
    val padding: Dp,
    val nameGap: Dp,
    val nameStyle: (Typography) -> TextStyle,
)

private val POD_METRICS = mapOf(
    PodSize.LARGE to PodMetrics(avatar = 190.dp, padding = 24.dp, nameGap = 16.dp, nameStyle = { it.headlineSmall }),
    PodSize.SMALL to PodMetrics(avatar = 120.dp, padding = 16.dp, nameGap = 10.dp, nameStyle = { it.titleMedium }),
)

/** Pods other than the one signing in fade while the screen is busy. */
private val BUSY_ALPHA = mapOf(true to 1f, false to 0.38f)

private val POD_SHAPE = RoundedCornerShape(28.dp)

/** A user's picture and name; a click signs the user in. */
@Composable
fun UserPod(user: LoginUser, size: PodSize, busy: Busy?, avatars: UserAvatars, onClick: () -> Unit) {
    val metrics = POD_METRICS.getValue(size)
    val name = user.name ?: stringResource(R.string.unnamed_user)
    val description = stringResource(R.string.sign_in_as, name)
    val isSigningIn = busy == Busy.SigningIn(user.id)
    val interactions = remember { MutableInteractionSource() }
    val isHovered by interactions.collectIsHoveredAsState()
    val isFocused by interactions.collectIsFocusedAsState()
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = HIGHLIGHT_ALPHA.getValue(isHovered || isFocused))

    Column(
        modifier = Modifier
            .alpha(BUSY_ALPHA.getValue(busy == null || isSigningIn))
            .clip(POD_SHAPE)
            .background(highlight)
            .hoverable(interactions)
            .selectable(
                selected = isSigningIn,
                enabled = busy == null,
                role = Role.Button,
                interactionSource = interactions,
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = description }
            .padding(metrics.padding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Avatar(user.id, metrics.avatar, avatars)
            if (isSigningIn) SigningInIndicator(metrics.avatar)
        }
        Spacer(Modifier.height(metrics.nameGap))
        Text(
            text = name,
            modifier = Modifier.width(metrics.avatar),
            style = metrics.nameStyle(MaterialTheme.typography),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val HIGHLIGHT_ALPHA = mapOf(true to 0.12f, false to 0f)

@Composable
private fun Avatar(userId: Int, size: Dp, avatars: UserAvatars) {
    val bitmap by produceState<Bitmap?>(initialValue = null, userId) {
        value = withContext(Dispatchers.IO) { avatars.load(userId) }
    }
    val modifier = Modifier.size(size).clip(CircleShape)
    bitmap?.let {
        Image(it.asImageBitmap(), contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    } ?: Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest))
}

@Composable
private fun SigningInIndicator(size: Dp) {
    val description = stringResource(R.string.signing_in)
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size / 3).semantics { contentDescription = description },
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
