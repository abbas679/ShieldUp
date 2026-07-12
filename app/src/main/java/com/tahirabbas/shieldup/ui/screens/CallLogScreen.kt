package com.tahirabbas.shieldup.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.CallLogRepository
import com.tahirabbas.shieldup.data.SuspiciousCallEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    logRepository: CallLogRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(logRepository.getEntries()) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suspicious Call Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No calls logged yet.", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries) { entry ->
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(entry.claimedCaller, style = MaterialTheme.typography.titleMedium)
                            if (entry.phoneNumber.isNotBlank()) {
                                Text(entry.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                dateFormat.format(Date(entry.timestampMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color.Gray
                            )
                            if (entry.redFlags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("${entry.redFlags.size} warning sign(s) noted", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            }
                            if (entry.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                TextButton(onClick = { shareEntry(context, entry, dateFormat.format(Date(entry.timestampMillis))) }) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share")
                                }
                                TextButton(onClick = {
                                    logRepository.removeEntry(entry)
                                    entries = logRepository.getEntries()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareEntry(context: android.content.Context, entry: SuspiciousCallEntry, dateStr: String) {
    val flagsText = entry.redFlags.joinToString("\n") { "- ${it.description}" }
    val body = buildString {
        append("Suspicious call logged via ShieldUp\n")
        append("Claimed to be: ${entry.claimedCaller}\n")
        if (entry.phoneNumber.isNotBlank()) append("Number: ${entry.phoneNumber}\n")
        append("Time: $dateStr\n")
        if (entry.redFlags.isNotEmpty()) append("Warning signs:\n$flagsText\n")
        if (entry.notes.isNotBlank()) append("Notes: ${entry.notes}\n")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "Share suspicious call"))
}
