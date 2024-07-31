package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItemButton
import com.itsha123.autosilent.singletons.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController? = null, context: Context? = null) {
    Column {
        MediumTopAppBar(
            title = { Text(stringResource(R.string.help_settings_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingsItemButton(
                    stringResource(R.string.faq_page_title),
                    Icons.AutoMirrored.Rounded.Help
                ) {
                    navController?.navigate(Routes.FAQ)
                }
                SettingsItemButton(
                    stringResource(R.string.feedback_button_text),
                    Icons.Rounded.Feedback
                ) {
                    startActivity(
                        context!!, Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(context.getString(R.string.app_issues_link))
                        ), null
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    HelpScreen()
}