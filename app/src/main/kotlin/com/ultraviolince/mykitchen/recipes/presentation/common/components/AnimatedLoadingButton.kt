package com.ultraviolince.mykitchen.recipes.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun AnimatedLoadingButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    defaultIcon: ImageVector,
    contentDescriptionText: String,
    modifier: Modifier = Modifier,
    loadingIcon: ImageVector = Icons.Default.Autorenew
) {
    FloatingActionButton(
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        modifier = modifier.semantics { contentDescription = contentDescriptionText }
    ) {
        if (isLoading) {
            val rotationAnimatable = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                rotationAnimatable.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }

            Box(modifier = Modifier.rotate(rotationAnimatable.value)) {
                Icon(
                    imageVector = loadingIcon,
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        } else {
            Icon(
                imageVector = defaultIcon,
                contentDescription = stringResource(id = R.string.save)
            )
        }
    }
}

class AnimatedLoadingButtonPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@Preview(showBackground = true)
@Composable
internal fun AnimatedLoadingButtonPreview(
    @PreviewParameter(AnimatedLoadingButtonPreviewParameterProvider::class) isLoading: Boolean
) {
    MyApplicationTheme {
        AnimatedLoadingButton(
            onClick = {},
            isLoading = isLoading,
            defaultIcon = Icons.Default.Done,
            contentDescriptionText = "Sample loading button"
        )
    }
}
