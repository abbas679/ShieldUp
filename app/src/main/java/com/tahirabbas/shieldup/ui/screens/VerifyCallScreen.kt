package com.tahirabbas.shieldup.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.CallLogRepository
import com.tahirabbas.shieldup.data.FamilyContact
import com.tahirabbas.shieldup.data.FamilyContactRepository
import com.tahirabbas.shieldup.data.RedFlag

private sealed class VerifyState {
    object Filling : VerifyState()
    data class Verdict(val checkedCount: Int, val entryId: String, val matchedContact: FamilyContact?) : VerifyState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCallScreen(
    logRepository: CallLogRepository,
    contactRepository: FamilyContactRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val savedContacts = remember { contactRepository.getContacts() }

    var selectedContact by remember { mutableStateOf<FamilyContact?>(null) }
    var claimedCaller by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val checkedFlags = remember { mutableStateMapOf<RedFlag, Boolean>() }
    var state by remember { mutableStateOf<VerifyState>(VerifyState.Filling) }

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
        when (val current = state) {
            is VerifyState.Filling -> {
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
                                    "Ask them for your family codeword now, from memory. This app will never show it here, that's intentional.",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (savedContacts.isNotEmpty()) {
                        item { Text("Who do they claim to be?", style = MaterialTheme.typography.titleMedium) }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                savedContacts.forEach { contact ->
                                    FilterChip(
                                        selected = selectedContact == contact,
                                        onClick = {
                                            selectedContact = contact
                                            claimedCaller = contact.name
                                            phoneNumber = contact.phoneNumber
                                        },
                                        label = { Text(contact.name) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = claimedCaller,
                            onValueChange = { claimedCaller = it; selectedContact = null },
                            label = { Text("Name (or type someone not in your list)") },
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
                                val entry = logRepository.addEntry(
                                    claimedCaller = claimedCaller.ifBlank { "Unknown" },
                                    phoneNumber = phoneNumber,
                                    redFlags = flags,
                                    notes = notes
                                )
                                state = VerifyState.Verdict(checkedCount, entry.id, selectedContact)
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("Get My Verdict")
                        }
                    }
                }
            }

            is VerifyState.Verdict -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val riskColor = when {
                        current.checkedCount >= 3 -> Color(0xFFC53030)
                        current.checkedCount >= 1 -> Color(0xFFB7791F)
                        else -> Color(0xFF2F855A)
                    }
                    val riskLabel = when {
                        current.checkedCount >= 3 -> "High caution advised"
                        current.checkedCount >= 1 -> "Some warning signs"
                        else -> "No red flags noted"
                    }
                    Text(riskLabel, style = MaterialTheme.typography.headlineMedium, color = riskColor, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (current.checkedCount >= 3)
                            "Hang up. Call them back directly on a number you already know and trust, not the number that just called you."
                        else if (current.checkedCount >= 1)
                            "Worth a second check. If anything felt off, verify through a separate, known channel before acting."
                        else
                            "Still, always confirm anything urgent or money-related through a channel you already trust.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (current.matchedContact != null) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${current.matchedContact.phoneNumber}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call ${current.matchedContact.name} on their saved number")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth()) {
                        Text("Done, view call log")
                    }
                }
            }
        }
    }
}
