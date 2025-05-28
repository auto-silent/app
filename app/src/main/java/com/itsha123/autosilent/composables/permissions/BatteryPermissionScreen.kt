package com.itsha123.autosilent.composables.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.singletons.Variables.recompose
import com.itsha123.autosilent.ui.theme.AutoSilentTheme

@Composable
fun BatteryPermissionRequestScreen(
    navController: NavController? = null,
    onRequestPermission: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold { innerPadding ->
        recompose.collectAsState().value
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.battery_permission_rationale),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                onRequestPermission()
            }) {
                Text(
                    stringResource(R.string.grant_permission)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            TextButton(onClick = { showDialog = true }) {
                Text(
                    stringResource(R.string.notifications_permission_reject_button),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.are_you_sure)) },
            confirmButton = {
                TextButton(onClick = { navController?.popBackStack() }) {
                    Text(
                        stringResource(R.string.sure)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) { Text(stringResource(R.string.close)) }
            },
            text = { Text(stringResource(R.string.battery_permission_dialog_text)) })
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PermissionPreviewBattery() {
    AutoSilentTheme {
        BatteryPermissionRequestScreen {}
    }
}