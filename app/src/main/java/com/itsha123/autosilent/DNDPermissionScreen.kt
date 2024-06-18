package com.itsha123.autosilent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itsha123.autosilent.ui.theme.AutoSilentTheme
@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun DNDPermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Please grant Do Not Disturb permission to the app, it is required to change the phone's ringer mode.", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
        Button(onClick = {
            onRequestPermission()
        }) {
            Text("Grant Permission")
        }
    }
}
@RequiresApi(Build.VERSION_CODES.M)
@Preview(showBackground = true)
@Composable
fun PermissionPreviewDND() {
    AutoSilentTheme {
        DNDPermissionRequestScreen {}
    }
}