import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.composables.settings.components.SettingsItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    Column {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingsItem(
                    title = stringResource(R.string.general_settings_title),
                    icon = Icons.Rounded.Settings,
                    navController = navController,
                    navigateTo = "general_settings",
                    description = stringResource(R.string.general_settings_desc)
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.cache_settings_title),
                    description = stringResource(R.string.cache_settings_desc),
                    icon = Icons.Rounded.Save,
                    navController = navController,
                    navigateTo = "cache_settings"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen()
}
