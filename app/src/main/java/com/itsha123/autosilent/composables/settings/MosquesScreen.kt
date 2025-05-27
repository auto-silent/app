package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.singletons.Routes
import com.itsha123.autosilent.singletons.Variables.internet
import com.itsha123.autosilent.utilities.getRequests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosquesScreen(navController: NavController? = null, context: Context? = null) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        LargeTopAppBar(
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = { Text(stringResource(R.string.add_mosques)) },
            navigationIcon = {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(
                            R.string.back
                        )
                    )
                }
            }
        )
    }) { innerPadding ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                Text(
                    stringResource(R.string.added_mosques_heading),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                val requests = getRequests(context)
                val statusMap = remember { mutableStateMapOf<Int, Int>() }

                requests.forEach { request ->
                    LaunchedEffect(request.issueNumber) {
                        try {
                            val status = getStatus(context!!, request.issueNumber)
                            statusMap[request.issueNumber] = status
                        } catch (e: Exception) {
                            Log.e("StatusLoad", "Failed to load status", e)
                            if (!internet.value) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context!!.getString(R.string.no_internet_snackbar_message)
                                    )
                                }
                            } else if (e.message?.contains("Rate limit") == true) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context!!.getString(R.string.rate_limit_exceeded_snackbar_message)
                                    )
                                }
                            }
                            statusMap[request.issueNumber] = 7
                        }
                    }

                    MasjidCard(
                        title = request.title,
                        address = request.address,
                        latitude = request.latitude,
                        longitude = request.longitude,
                        status = statusMap[request.issueNumber] ?: 1,
                        issueNumber = request.issueNumber,
                        context = context
                    )
                }
            }
            item {
                TextButton(onClick = { showDialog = true }) {
                    Text(stringResource(R.string.missing_mosques))
                }
            }
            item {
                Button(onClick = { navController?.navigate(Routes.ADDMOSQUES) }) {
                    Text(stringResource(R.string.add_mosques))
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                text = { Text(stringResource(R.string.missing_mosques_dialog)) },
                confirmButton = {
                    TextButton(onClick = {
                        context?.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                context.getString(R.string.database_github_link).toUri()
                            ), null
                        )
                    }) {
                        Text(stringResource(R.string.open_github))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}

@Composable
fun MasjidCard(
    title: String,
    address: String,
    latitude: String,
    longitude: String,
    status: Int,
    issueNumber: Int,
    context: Context? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val (statusText, statusColor, statusBgColor) = when (status) {
        1 -> Triple("Pending", Color(0xFF5900FF), Color(0xFFD3BDFF))
        2 -> Triple("Assigned", Color(0xFFFF6F00), Color(0xFFFFC599))
        3 -> Triple("In Progress", Color(0xFFFFE500), Color(0xFFFFE7BE))
        4 -> Triple("Completed", Color(0xFF00D000), Color(0xFFD2FFD2))
        5 -> Triple("Rejected", Color(0xFFFF4C4C), Color(0xFFFFE0E0))
        6 -> Triple("Duplicate", Color.Black, Color.LightGray)
        else -> Triple("Error", Color.Gray, Color.LightGray)
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = statusText, color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Address: ") }
                append(address)
            })
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Latitude: ") }
                append(latitude)
            })
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Longitude: ") }
                append(longitude)
            })
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = { Text(stringResource(R.string.open_request_in_github)) },
            confirmButton = {
                TextButton(onClick = {
                    val url = "https://github.com/auto-silent/database/issues/$issueNumber"
                    context?.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    showDialog = false
                }) {
                    Text(stringResource(R.string.open_github))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

object RateLimitTracker {
    fun updateResetTime(context: Context, resetTime: Long) {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        sharedPref.edit { putLong("rateLimitTime", resetTime) }
    }

    fun canMakeRequest(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val retryAfter = sharedPref.getLong("rateLimitTime", 0)
        return System.currentTimeMillis() / 1000 >= retryAfter
    }

    fun getRetryTime(context: Context): Long {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("rateLimitTime", 0)
    }
}

suspend fun getStatus(context: Context, issueNumber: Int): Int {
    return withContext(Dispatchers.IO) {
        if (!RateLimitTracker.canMakeRequest(context)) {
            throw Exception("Rate limited. Retry after ${RateLimitTracker.getRetryTime(context)} (epoch seconds).")
        }

        val client = OkHttpClient()
        val owner = "auto-silent"
        val repo = "database"
        val url = "https://api.github.com/repos/$owner/$repo/issues/$issueNumber"

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.header("X-RateLimit-Remaining") == "0") {
                val reset = response.header("X-RateLimit-Reset")?.toLongOrNull()
                if (reset != null) {
                    RateLimitTracker.updateResetTime(context, reset)
                    throw Exception("Rate limit exceeded. Retry after $reset (epoch seconds).")
                }
            }

            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            val body = response.body?.string() ?: throw Exception("Empty body")
            val json = JSONObject(body)
            Log.d("MasjidStatus", "Headers: ${response.headers}")
            val state = json.getString("state")
            val assigneeExists = !json.isNull("assignee")

            val timelineUrl = "$url/timeline"
            val timelineRequest = Request.Builder()
                .url(timelineUrl)
                .header("Accept", "application/vnd.github.mockingbird-preview+json")
                .build()

            val linked = client.newCall(timelineRequest).execute().use { timelineResponse ->
                if (timelineResponse.header("X-RateLimit-Remaining") == "0") {
                    val reset = timelineResponse.header("X-RateLimit-Reset")?.toLongOrNull()
                    if (reset != null) {
                        RateLimitTracker.updateResetTime(context, reset)
                        throw Exception("Rate limit exceeded on timeline. Retry after $reset.")
                    }
                }
                if (!timelineResponse.isSuccessful) {
                    false
                } else {
                    val timelineJson = timelineResponse.body?.string()
                    timelineJson?.contains("referenced") ?: false
                }
            }

            when {
                state == "closed" && json.optString("state_reason") == "completed" -> 4
                state == "closed" && json.optString("state_reason") == "not_planned" -> 5
                state == "closed" && json.optString("state_reason") == "duplicate" -> 6
                linked -> 3
                assigneeExists -> 2
                else -> 1
            }
        }
    }
}

@Preview
@Composable
fun PreviewMosquesScreen() {
    MosquesScreen()
}