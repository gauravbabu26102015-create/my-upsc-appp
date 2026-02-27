package com.myguruu.app.features.alarm

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyRoutineScreen() {
    val context = LocalContext.current
    val alarmScheduler = remember { AlarmScheduler(context) }
    
    var routines by remember { mutableStateOf(listOf<RoutineItem>()) }
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    showDialog = true
                }
            }) {
                Icon(Icons.Filled.Add, "Add Routine")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "My Study Routine",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (routines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No routines scheduled. Tap + to add!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(routines) { item ->
                        RoutineCard(
                            item = item,
                            onDelete = {
                                alarmScheduler.cancel(item)
                                routines = routines.filter { it.id != item.id }
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddRoutineDialog(
                onDismiss = { showDialog = false },
                onAdd = { subject, hour, minute ->
                    val newItem = RoutineItem(
                        id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                        subject = subject,
                        hour = hour,
                        minute = minute
                    )
                    routines = routines + newItem
                    alarmScheduler.schedule(newItem)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutineDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Session") },
        text = {
            Column {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Physics)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:MM 24hr)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parts = time.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull() ?: 8
                    val m = parts[1].toIntOrNull() ?: 0
                    onAdd(subject, h, m)
                }
            }) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RoutineCard(item: RoutineItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = item.subject, style = MaterialTheme.typography.titleLarge)
                Text(text = String.format("%02d:%02d", item.hour, item.minute), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove")
            }
        }
    }
}
