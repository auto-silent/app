package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.itsha123.autosilent.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(context: Context? = null) {
    val showDialog = remember { mutableStateOf(false) }
    Scaffold { innerPadding ->
        val options = listOf(
            stringResource(R.string.bug_report),
            stringResource(R.string.feature_request),
            stringResource(R.string.other_feedback)
        )
        var text by remember { mutableStateOf<String?>(options[0]) }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.feedback_type_prompt),
                modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center
            )
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
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
                            text = {
                                Text(
                                    option,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                text = option
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                },
                text = { Text(text = stringResource(R.string.no_github_dialog)) },
                confirmButton = {
                    TextButton(onClick = {
                        context?.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data =
                                "mailto:autosilentapp@gmail.com".toUri()
                        })
                    }) { Text(stringResource(R.string.send_email)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog.value = false
                    }) { Text(stringResource(R.string.close)) }
                }
            )
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (text == options[0] || text == options[1]) {
                TextButton(onClick = {
                    showDialog.value = true
                }) {
                    Text(text = stringResource(R.string.no_github))
                }
            } else {
                Text(
                    text = stringResource(R.string.feedback_send_email_prompt),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (text == options[0] || text == options[1]) {
                Button(onClick = {
                    context?.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            context.getString(R.string.app_issues_link).toUri()
                        )
                    )
                }) {
                    Text(text = stringResource(R.string.open_github))
                }
            } else {
                Button(onClick = {
                    context?.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                        data =
                            "mailto:autosilentapp@gmail.com".toUri()
                    })
                }) {
                    Text(text = stringResource(R.string.send_email))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    FeedbackScreen()
}
