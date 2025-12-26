package com.itsha123.autosilent.composables.settings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.itsha123.autosilent.R
import com.itsha123.autosilent.singletons.Variables.internet
import com.itsha123.autosilent.utilities.RequestData
import com.itsha123.autosilent.utilities.saveRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun AddMosquesScreen(context: Context? = null, navController: NavController? = null) {
    val sharedPref = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            var name by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var latitude by remember { mutableStateOf("") }
            var longitude by remember { mutableStateOf("") }
            var allFilled by remember { mutableStateOf(true) }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                isError = !allFilled && name == "",
                label = {
                    Text(
                        stringResource(R.string.name)
                    )
                },
                supportingText = {
                    if (!allFilled && name == "") {
                        Text(
                            text = stringResource(R.string.mosque_name_required)
                        )
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                isError = !allFilled && address == "",
                label = {
                    Text(
                        stringResource(R.string.address)
                    )
                },
                supportingText = {
                    if (!allFilled && address == "") {
                        Text(
                            text = stringResource(R.string.mosque_address_required)
                        )
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
            Row {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    isError = (!allFilled && latitude == "") || (latitude != "" && !latitude.matches(
                        Regex("^-?\\d+\\.\\d{5,}\$")
                    )),
                    label = {
                        Text(
                            stringResource(R.string.latitude)
                        )
                    },
                    supportingText = {
                        if (!allFilled && latitude == "") {
                            Text(
                                text = stringResource(R.string.mosque_latitude_required)
                            )
                        } else if (latitude != "" && !latitude.matches(Regex("^-?\\d+\\.\\d{5,}\$"))) {
                            Text(
                                text = stringResource(R.string.invalid_latitude_longitude)
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(0.45f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    isError = (!allFilled && longitude == "") || (longitude != "" && !longitude.matches(
                        Regex("^-?\\d+\\.\\d{5,}\$")
                    )),
                    label = {
                        Text(
                            stringResource(R.string.longitude)
                        )
                    },
                    supportingText = {
                        if (!allFilled && longitude == "") {
                            Text(
                                text = stringResource(R.string.mosque_longitude_required)
                            )
                        } else if (longitude != "" && !longitude.matches(Regex("^-?\\d+\\.\\d{5,}\$"))) {
                            Text(
                                text = stringResource(R.string.invalid_latitude_longitude)
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                )
            }
            Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = {
                        if (System.currentTimeMillis() - sharedPref!!.getLong(
                                "mosqueRequestTimestamp",
                                0
                            ) < 60000
                        ) {
                            val secondsRemaining =
                                ((60000 - (System.currentTimeMillis() - sharedPref.getLong(
                                    "mosqueRequestTimestamp",
                                    0
                                ))) / 1000).toInt()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Wait $secondsRemaining seconds until you can submit another request"
                                )
                            }
                            return@Button
                        }
                        if (name.isEmpty() || address.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
                            allFilled = false
                            return@Button
                        }
                        if (!latitude.matches(Regex("^-?\\d+\\.\\d{5,}\$")) || !longitude.matches(
                                Regex("^-?\\d+\\.\\d{5,}\$")
                            )
                        ) {
                            return@Button
                        }
                        GlobalScope.launch(Dispatchers.IO) {
                            sharedPref.edit {
                                putLong("mosqueRequestTimestamp", System.currentTimeMillis())
                            }
                            val client = OkHttpClient()
                            val mediaType = "application/json; charset=utf-8".toMediaType()
                            val api = "https://itsha123.pythonanywhere.com/create_issue"
                            val json = """
                                {
                                    "title": "Add $name",
                                    "body": "### Add $name\n**Address:** $address\n**Latitude:** $latitude\n**Longitude:** $longitude",
                                    "labels": ["enhancement"]
                                }
                            """
                            val body = json.toRequestBody(mediaType)

                            val request = Request.Builder()
                                .url(api)
                                .post(body)
                                .build()

                            try {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.submitting),
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                                client.newCall(request).execute().use { response ->
                                    if (response.isSuccessful) {
                                        val responseBody = response.body?.string()
                                        if (responseBody != null) {
                                            val jsonResponse = JSONObject(responseBody)
                                            Log.d("", "Parsed Response: $jsonResponse")
                                            val issueNumber =
                                                jsonResponse.getJSONObject("id").getInt("number")
                                            val requestData = RequestData(
                                                issueNumber = issueNumber,
                                                title = name,
                                                address = address,
                                                latitude = latitude,
                                                longitude = longitude
                                            )
                                            saveRequest(context, requestData)
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                            }
                                            showDialog = true
                                        } else {
                                            if (!internet.value) {
                                                scope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(
                                                        context.getString(
                                                            R.string.no_internet_snackbar_message
                                                        )
                                                    )
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(
                                                        context.getString(
                                                            R.string.something_went_wrong
                                                        )
                                                    )
                                                }
                                            }
                                            Log.d("", "Response body is null")
                                        }
                                    } else {
                                        if (!internet.value) {
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(context.getString(R.string.no_internet_snackbar_message))
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(context.getString(R.string.something_went_wrong))
                                            }
                                        }
                                        Log.d("", "Request failed with code: ${response.code}")
                                        Log.d("", "Error message: ${response.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                if (!internet.value) {
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(context.getString(R.string.no_internet_snackbar_message))
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(context.getString(R.string.something_went_wrong))
                                    }
                                }
                                Log.d("", "Error sending request: ${e.message}")
                            }
                        }
                    },
                    content = { Text(stringResource(R.string.submit)) },
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                text = { Text(stringResource(R.string.mosque_submitted)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        name = ""
                        address = ""
                        latitude = ""
                        longitude = ""
                        allFilled = true
                    }) { Text(stringResource(R.string.continue_text)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController?.popBackStack()
                    }) { Text(stringResource(R.string.go_back)) }
                })
        }
    }
}

@Preview
@Composable
fun AddMosquesScreenPreview() {
    AddMosquesScreen()
}