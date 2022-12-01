package com.dev.headphoneplayer.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.headphoneplayer.R
import com.dev.headphoneplayer.ui.theme.Pink500

@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 26.dp
) {

    val transition = updateTransition(targetState = isLiked, label = "transition")

    val size by transition.animateDp(transitionSpec = {
        keyframes {
            durationMillis = 500
            buttonSize + 10.dp at 250 with LinearOutSlowInEasing
            buttonSize at 400 with LinearOutSlowInEasing
        }
    }, label = "size",
        targetValueByState = { state ->
            if (state) buttonSize else buttonSize
        }
    )

    val shouldBeAnimated = remember { mutableStateOf(value = false) }

    Icon(
        painter = painterResource(id = if (isLiked) R.drawable.heart_solid else R.drawable.heart_outlined),
        contentDescription = "",
        tint = if (enabled) Pink500 else MaterialTheme.colors.onSurface,
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 100.dp))
            .padding(all = 8.dp)
            .clickable(
                indication = rememberRipple(bounded = false),
                enabled = enabled,
                onClick = {
                    shouldBeAnimated.value = true
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Button
            )
            .size(size = if (shouldBeAnimated.value) size else buttonSize)
    )

}
