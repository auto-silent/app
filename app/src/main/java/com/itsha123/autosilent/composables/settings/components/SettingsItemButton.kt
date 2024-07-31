package com.itsha123.autosilent.composables.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsItemButton(title: String, icon: ImageVector? = null, onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(targetValue = if (clicked) 0.95f else 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    clicked = true
                    onClick()
                },
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .scale(scale.value)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
            Text(text = title, fontSize = 20.sp)
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            clicked = false
        }
    }
}