package com.tahirabbas.shieldup.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.utils.ClipboardCheckHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardCheckScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var result by remember { mutableStateOf<ClipboardCheckHelper.ClipboardResult?>(null) }
    var maskedPreview by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clipboard Check") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Before pasting something into a chat, form, or app you're not sure about, " +
                        "check what's actually on your clipboard right now. Checks for card numbers, " +
                        "one-time codes, passwords, and email addresses.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Button(onClick = {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = clipboardManager.primaryClip
                    ?.takeIf { it.itemCount > 0 }
                    ?.getItemAt(0)?.text?.toString() ?: ""
                result = ClipboardCheckHelper.check(text)
                maskedPreview = if (text.isNotBlank()) ClipboardCheckHelper.maskForPreview(text) else null
                checked = true
            }) {
                Text("Check My Clipboard Now")
            }

            if (checked) {
                Spacer(modifier = Modifier.height(24.dp))
                val res = result
                val isSensitive = res?.isSensitive == true

                if (maskedPreview != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("On your clipboard:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(maskedPreview!!, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSensitive) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSensitive) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isSensitive) Color(0xFFB7791F) else Color(0xFF2F855A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            res?.reason ?: "Nothing sensitive looking found on your clipboard right now.",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
