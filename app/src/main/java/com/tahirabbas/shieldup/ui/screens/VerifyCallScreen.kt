package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.CallLogRepository
import com.tahirabbas.shieldup.data.RedFlag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCallScreen(
    logRepository: CallLogRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var claimedCaller by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val checkedFlags = remember { mutableStateMapOf<RedFlag, Boolean>() }

    val checkedCount = checkedFlags.values.count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify This Call") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Ask them for your family codeword now, from memory. " +
                                    "This app will never show it here — that's intentional.",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = claimedCaller, onValueChange = { claimedCaller = it },
                    label = { Text("Who do they claim to be?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = phoneNumber, onValueChange = { phoneNumber = it },
                    label = { Text("Number they're calling from") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Check anything that applies:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }

            items(RedFlag.values().toList()) { flag ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    onClick = { checkedFlags[flag] = !(checkedFlags[flag] ?: false) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedFlags[flag] ?: false,
                            onCheckedChange = { checkedFlags[flag] = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(flag.description, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                if (checkedCount > 0) {
                    val riskColor = if (checkedCount >= 3) Color(0xFFC53030) else Color(0xFFB7791F)
                    val riskText = if (checkedCount >= 3)
                        "$checkedCount warning signs present — treat with real caution. Hang up and call them back on their known number."
                    else
                        "$checkedCount warning sign(s) noted — worth a second check before acting."
                    Card(colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))) {
                        Text(
                            riskText,
                            modifier = Modifier.padding(16.dp),
                            color = riskColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val flags = checkedFlags.filterValues { it }.keys.toList()
                        logRepository.addEntry(
                            claimedCaller = claimedCaller.ifBlank { "Unknown" },
                            phoneNumber = phoneNumber,
                            redFlags = flags,
                            notes = notes
                        )
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Save to Call Log")
                }
            }
        }
    }
}
