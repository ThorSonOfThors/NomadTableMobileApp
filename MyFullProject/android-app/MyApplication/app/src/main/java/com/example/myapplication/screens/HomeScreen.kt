package com.example.myapplication.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Activity
import com.example.myapplication.models.CreateActivityRequest
import com.example.myapplication.network.RetrofitInstance
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime



// ---------------- MODELS ----------------



// ---------------- SCREEN ----------------

@Composable
fun HomeScreen(userId: Long?) {

    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var selectedActivity by remember { mutableStateOf<Activity?>(null) }

    // load activities
    LaunchedEffect(Unit) {
        RetrofitInstance.api.getActivities()
            .enqueue(object : Callback<List<Activity>> {
                override fun onResponse(
                    call: Call<List<Activity>>,
                    response: Response<List<Activity>>
                ) {
                    if (response.isSuccessful) {
                        activities = response.body() ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {}
            })
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---------------- MAP (OSM) ----------------

        AndroidView(
            factory = { ctx ->

                Configuration.getInstance().load(
                    ctx,
                    ctx.getSharedPreferences("osm", Context.MODE_PRIVATE)
                )

                MapView(ctx).apply {

                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    controller.setZoom(12.0)
                    controller.setCenter(
                        GeoPoint(-6.2088, 106.8456) // Jakarta
                    )
                }
            },
            update = { mapView ->

                mapView.overlays.clear()

                activities.forEach { activity ->

                    val marker = Marker(mapView)
                    marker.position = GeoPoint(
                        activity.latitude,
                        activity.longitude
                    )

                    marker.title = activity.title
                    marker.snippet = activity.description

                    marker.setOnMarkerClickListener { _, _ ->
                        selectedActivity = activity
                        true
                    }

                    mapView.overlays.add(marker)
                }

                mapView.invalidate()
            }
        )

        // ---------------- FAB ----------------

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }
    }

    // ---------------- DIALOG ----------------

    if (showDialog) {
        CreateActivityDialog(
            userId = userId,
            onDismiss = { showDialog = false },
            onCreated = { newActivity ->
                activities = activities + newActivity
            }
        )
    }

    selectedActivity?.let { activity ->

        AlertDialog(
            onDismissRequest = {
                selectedActivity = null
            },

            title = {
                Text(activity.creatorName +" wants to " +  activity.title)
            },

            text = {
                Column {

                    Text(activity.description)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Time: ${activity.eventTime}")
                    //Text("Longitude: ${activity.longitude}")
                }
            },

            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    ),
                    onClick = {

                        if (userId != null) {

                            RetrofitInstance.api.joinActivity(
                                activity.activityId,
                                userId
                            ).enqueue(object : Callback<Activity> {

                                override fun onResponse(
                                    call: Call<Activity>,
                                    response: Response<Activity>
                                ) {
                                    if (response.isSuccessful) {
                                        selectedActivity = null
                                    }
                                }

                                override fun onFailure(
                                    call: Call<Activity>,
                                    t: Throwable
                                ) {
                                    // Handle failure if desired
                                }
                            })
                        }
                    }
                ) {
                    Text("Join Activity")
                }
            },

            dismissButton = {
                Button(
                    onClick = {
                        selectedActivity = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

// ---------------- DIALOG ----------------

@Composable
fun CreateActivityDialog(
    userId: Long?,
    onDismiss: () -> Unit,
    onCreated: (Activity) -> Unit
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val context = LocalContext.current

    val timePicker = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
            },
            12,
            0,
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Activity") },
        text = {

            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )

                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") }
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { timePicker.show() }) {
                    Text(selectedTime?.toString() ?: "Pick Time")
                }
            }
        },
        confirmButton = {
            Button(onClick = {

                if (userId == null) return@Button

                val eventTime = LocalDateTime.of(
                    LocalDate.now(),
                    selectedTime ?: LocalTime.now()
                )

                val request = CreateActivityRequest(
                    creatorId = userId,
                    title = title,
                    description = description,
                    latitude = latitude.toDoubleOrNull() ?: 0.0,
                    longitude = longitude.toDoubleOrNull() ?: 0.0,
                    eventTime = eventTime.toString()
                )

                RetrofitInstance.api.createActivity(request)
                    .enqueue(object : Callback<Activity> {
                        override fun onResponse(
                            call: Call<Activity>,
                            response: Response<Activity>
                        ) {
                            response.body()?.let {
                                onCreated(it)
                            }
                        }

                        override fun onFailure(call: Call<Activity>, t: Throwable) {}
                    })

                onDismiss()
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}