package com.itsha123.autosilent.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.singletons.Variables.internet
import com.itsha123.autosilent.singletons.Variables.service
import com.itsha123.autosilent.ui.theme.AutoSilentTheme

@Composable
fun UI(
    onClick: () -> Unit,
    geofenceText: String,
    inGeofence: Boolean,
    link: () -> Unit,
    navController: NavController
) {
    Scaffold(floatingActionButton = {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                modifier = Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
        }
    }, floatingActionButtonPosition = FabPosition.Start) { innerPadding ->
        val showDialog = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = geofenceText,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (!inGeofence && internet.collectAsState().value) {
                TextButton(onClick = { showDialog.value = true }) {
                    Text(
                        stringResource(R.string.in_mosque_question),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(onClick = {
                onClick()
            }, modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    if (service.collectAsState().value) stringResource(R.string.turn_off) else stringResource(
                        R.string.turn_on
                    )
                )
            }
            if (showDialog.value) {
                AlertDialog(onDismissRequest = { showDialog.value = false }, text = {
                    Text(stringResource(R.string.mosque_not_in_database_popup))
                }, confirmButton = {
                    TextButton(onClick = {
                        link()
                        showDialog.value = false
                    }) {
                        Text(stringResource(R.string.add_mosque))
                    }
                }, dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(R.string.close))
                    }
                })

            }
        }
    }
}


@Composable
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
fun UIPreview() {
    AutoSilentTheme {
        UI({}, stringResource(R.string.not_in_mosque), false, {}, rememberNavController())
    }
}