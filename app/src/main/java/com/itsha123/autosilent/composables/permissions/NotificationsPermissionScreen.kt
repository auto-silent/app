package com.itsha123.autosilent.composables.permissions

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
fun NotificationsPermissionRequestScreen(
    navController: NavController? = null,
    context: Context? = null,
    onRequestPermission: () -> Unit
) {
    Scaffold { innerPadding ->
        val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        recompose.collectAsState().value
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.notifications_permission_rationale),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                onRequestPermission()
            }) {
                Text(
                    if ((sharedPref?.getInt("notificationRequests", 0) ?: 0) < 2) {
                        stringResource(R.string.grant_permission)
                    } else {
                        stringResource(R.string.open_settings_permission)
                    }
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
            TextButton(onClick = { navController?.popBackStack() }) {
                Text(
                    stringResource(R.string.notifications_permission_reject_button),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PermissionPreviewNotifications() {
    AutoSilentTheme {
        NotificationsPermissionRequestScreen {}
    }
}