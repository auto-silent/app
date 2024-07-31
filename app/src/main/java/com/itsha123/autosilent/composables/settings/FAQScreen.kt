package com.itsha123.autosilent.composables.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.itsha123.autosilent.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(navController: NavController? = null) {
    Column {
        MediumTopAppBar(
            title = { Text(stringResource(R.string.faq_page_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = stringResource(R.string.faq_question_how_to_disable_auto_silent),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.faq_answer_how_to_disable_auto_silent),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFAQScreen() {
    FAQScreen()
}