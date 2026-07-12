package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.utils.LinkCheckHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCheckerScreen(onBack: () -> Unit, sharedText: String? = null) {
    var input by remember { mutableStateOf(sharedText ?: "") }
    var result by remember { mutableStateOf<LinkCheckHelper.LinkCheckResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check a Link") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                "Paste a link, or the whole message it came in. This checks for known " +
                        "scam patterns, it's a helpful signal, not a guarantee.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it; result = null },
                label = { Text("Link or message text") },
                placeholder = { Text("e.g. \"Your account is suspended, verify now at bit.ly/xyz\"") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { result = LinkCheckHelper.analyze(input) },
                enabled = input.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check It")
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(20.dp))

                if (res.extractedUrl != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(res.extractedUrl, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val (color, label) = when (res.riskLevel) {
                    LinkCheckHelper.RiskLevel.HIGH -> Color(0xFFC53030) to "High caution advised"
                    LinkCheckHelper.RiskLevel.MEDIUM -> Color(0xFFB7791F) to "Some warning signs"
                    LinkCheckHelper.RiskLevel.LOW -> Color(0xFF2F855A) to "No obvious red flags found"
                }
                Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(label, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (res.warnings.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Still, always verify unexpected requests directly with the sender through a separate, known channel.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            res.warnings.forEach { warning ->
                                Text("• $warning", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
