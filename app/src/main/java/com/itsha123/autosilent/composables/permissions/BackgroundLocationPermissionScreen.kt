package com.itsha123.autosilent.composables.permissions

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itsha123.autosilent.R
import com.itsha123.autosilent.ui.theme.AutoSilentTheme

@Composable
fun BackgroundLocationPermissionRequestScreen(
    context: Context? = null,
    onRequestPermission: () -> Unit
) {
    Scaffold {
        val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.background_location_permission_rationale),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                onRequestPermission()
            }) {
                Text(
                    if (sharedPref!!.getInt("backgroundLocationRequests", 0) < 2) {
                        stringResource(R.string.grant_permission)
                    } else {
                        stringResource(R.string.open_settings_permission)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PermissionPreviewBackgroundLocation() {
    AutoSilentTheme {
        BackgroundLocationPermissionRequestScreen {}
    }
}