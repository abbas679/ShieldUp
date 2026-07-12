package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.ui.theme.TrustBlue

data class ShieldModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onVerifyCall: () -> Unit,
    onLinkChecker: () -> Unit,
    onSecurityCheckup: () -> Unit,
    onClipboardCheck: () -> Unit,
    onWifiScanner: () -> Unit,
    onCallLog: () -> Unit,
    onSettings: () -> Unit
) {
    val modules = listOf(
        ShieldModule("Verify a Call", "Family codeword + scam checklist", Icons.Default.VerifiedUser, TrustBlue, onVerifyCall),
        ShieldModule("Check a Link", "Scan a suspicious link or message", Icons.Default.Link, Color(0xFF6A1B9A), onLinkChecker),
        ShieldModule("Security Checkup", "Screen lock, root, WiFi & more", Icons.Default.Security, Color(0xFF00796B), onSecurityCheckup),
        ShieldModule("Clipboard Check", "Scan before you paste", Icons.Default.ContentPaste, Color(0xFFB7791F), onClipboardCheck),
        ShieldModule("Home WiFi Scan", "See devices on your network", Icons.Default.Wifi, Color(0xFF2F855A), onWifiScanner)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = TrustBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ShieldUp", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onCallLog) {
                        Icon(Icons.Default.History, contentDescription = "Call Log")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                Text(
                    "Pick what you want to check.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(modules) { module ->
                Card(
                    onClick = module.onClick,
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(module.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(module.icon, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(module.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(module.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
