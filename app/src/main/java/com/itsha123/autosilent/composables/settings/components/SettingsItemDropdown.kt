package com.itsha123.autosilent.composables.settings.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItemDropdown(
    title: String,
    context: Context? = null,
    syncedVariable: String,
    defValue: Boolean,
    options: List<String>,
    onCheck: () -> Unit
) {
    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    var clicked by remember { mutableStateOf(false) }
    val startOption = sharedPref?.getBoolean(syncedVariable, defValue)
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf<String?>(null) }
    if (startOption == true) {
        text = options[1]
    } else {
        text = options[0]
    }
    val scale = animateFloatAsState(targetValue = if (clicked) 0.95f else 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    clicked = true
                },
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .scale(scale.value)
    ) {
        Column {
            Text(text = title, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.weight(1f)) // Spacer with flexible weight to push the Switch to the right
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            TextField(
                // The `menuAnchor` modifier must be passed to the text field to handle
                // expanding/collapsing the menu on click. A read-only text field has
                // the anchor type `PrimaryNotEditable`.
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = text!!,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            text = option
                            expanded = false
                            onCheck()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            // Reset the click effect after navigation
            clicked = false
        }
    }
}
