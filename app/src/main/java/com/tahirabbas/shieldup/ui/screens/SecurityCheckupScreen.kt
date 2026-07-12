package com.tahirabbas.shieldup.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.utils.SecurityCheckHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCheckupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val checks = remember { SecurityCheckHelper.runAllChecks(context) }
    val passedCount = checks.count { it.passed }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Checkup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA))) {
                Text(
                    "$passedCount of ${checks.size} checks passed",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            checks.forEach { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            if (item.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (item.passed) Color(0xFF2F855A) else Color(0xFFC53030)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(item.title, fontWeight = FontWeight.SemiBold)
                            Text(item.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS))
                    } catch (e: Exception) {
                        // Some OEM builds don't expose this exact settings screen; fail silently
                        // rather than crash, since this is a convenience shortcut, not core functionality.
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open Android Privacy Dashboard", fontWeight = FontWeight.SemiBold)
                        Text(
                            "See which apps used your camera, mic, or location recently",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                }
            }
        }
    }
}
