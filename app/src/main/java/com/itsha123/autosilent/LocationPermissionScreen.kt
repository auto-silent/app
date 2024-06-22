package com.itsha123.autosilent

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

@Composable
fun LocationPermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Please grant Precise Location permission to the app, it is required for the app's core functionality.", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
        Button(onClick = {
            onRequestPermission()
        }) {
            Text("Grant Permission")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PermissionPreviewLocation() {
    AutoSilentTheme {
        LocationPermissionRequestScreen {}
    }
}