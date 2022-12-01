package com.dev.headphoneplayer.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoadingDialog(
    isLoading: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        securePolicy = SecureFlagPolicy.Inherit,
        usePlatformDefaultWidth = true,
        decorFitsSystemWindows = true
    )
) {
    if (isLoading) {
        Dialog(
            onDismissRequest = onDone,
            properties = properties,
            content = {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(all = 26.dp))
                }
            }
        )
    }
}
