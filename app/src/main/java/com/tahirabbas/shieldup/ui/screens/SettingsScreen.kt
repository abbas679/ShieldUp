package com.tahirabbas.shieldup.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.ApiKeyRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKeyRepository: ApiKeyRepository,
    onBack: () -> Unit,
    onOpenCodeword: () -> Unit = {},
    onOpenContacts: () -> Unit = {}
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(apiKeyRepository.getSafeBrowsingKey() ?: "") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Card(onClick = onOpenCodeword, modifier = Modifier.fillMaxWidth()) {
                SettingsRow(icon = Icons.Default.Key, title = "Family Codeword", subtitle = "Set or update your verification phrase")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(onClick = onOpenContacts, modifier = Modifier.fillMaxWidth()) {
                SettingsRow(icon = Icons.Default.People, title = "Family Contacts", subtitle = "Manage who this app helps you verify")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Link Checker: Safe Browsing", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Add a free Google Safe Browsing API key to check links against Google's live malware and phishing database, the same one Chrome uses. Without a key, the Link Checker only runs local wording pattern checks, which catch far less.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                label = { Text("Safe Browsing API key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    apiKeyRepository.setSafeBrowsingKey(apiKey)
                    saved = true
                }) { Text("Save Key") }
                OutlinedButton(onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://developers.google.com/safe-browsing/v4/get-started")
                        )
                    )
                }) { Text("How to get a key") }
            }
            if (saved) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Saved.", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("About ShieldUp", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "ShieldUp is a personal safety toolkit: verify suspicious calls with a family codeword, check suspicious links and messages, run a device security checkup, check your clipboard before pasting, and scan your home WiFi for unrecognized devices."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("What this app does NOT do", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "It does not record calls, read other apps' data, or monitor background activity. Every check here is either a manual, user initiated scan, a real Android system API, or, for the Link Checker with a key configured, a query to Google's own public threat database."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your codeword, contacts, call log, known devices, and API key are stored only on this device. The only network calls this app makes are the Safe Browsing lookup for a link you explicitly check, and the local WiFi scan of your own network."
            )
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
