package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenCodeword: () -> Unit = {},
    onOpenContacts: () -> Unit = {}
) {
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

            Spacer(modifier = Modifier.height(24.dp))

            Text("About ShieldUp", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "ShieldUp is a personal safety toolkit: verify suspicious calls with a family " +
                        "codeword, check suspicious links and messages, run a device security checkup, " +
                        "check your clipboard before pasting, and scan your home WiFi for unrecognized devices."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("What this app does NOT do", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "It does not record calls, read other apps' data, monitor background activity, or " +
                        "claim to automatically detect AI-cloned voices or hacking — those aren't things " +
                        "any legitimate app can reliably do. Every check here is either a manual, " +
                        "user-initiated scan, or a real Android system API — never a passive background scan."
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Everything — your codeword, contacts, call log, and known devices — is stored " +
                        "only on this device. Nothing is uploaded anywhere."
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
