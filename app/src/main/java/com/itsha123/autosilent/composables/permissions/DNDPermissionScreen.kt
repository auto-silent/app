package com.itsha123.autosilent.composables.permissions

import android.os.Build
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
fun DNDPermissionRequestScreen(onRequestPermission: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    stringResource(R.string.dnd_permission_rationale)
                } else {
                    stringResource(R.string.dnd_permission_rationale_legacy)
                },
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                onRequestPermission()
            }) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PermissionPreviewDND() {
    AutoSilentTheme {
        DNDPermissionRequestScreen {}
    }
}