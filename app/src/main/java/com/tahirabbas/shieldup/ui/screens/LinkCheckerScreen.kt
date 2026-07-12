package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.ApiKeyRepository
import com.tahirabbas.shieldup.utils.LinkCheckHelper
import com.tahirabbas.shieldup.utils.SafeBrowsingHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCheckerScreen(
    apiKeyRepository: ApiKeyRepository,
    onBack: () -> Unit,
    sharedText: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf(sharedText ?: "") }
    var result by remember { mutableStateOf<LinkCheckHelper.LinkCheckResult?>(null) }
    var safeBrowsingResult by remember { mutableStateOf<SafeBrowsingHelper.SafeBrowsingResult?>(null) }
    var checking by remember { mutableStateOf(false) }

    fun runCheck() {
        val res = LinkCheckHelper.analyze(input)
        result = res
        safeBrowsingResult = null
        val url = res.extractedUrl
        if (url != null) {
            checking = true
            scope.launch {
                val apiKey = apiKeyRepository.getSafeBrowsingKey()
                safeBrowsingResult = SafeBrowsingHelper.checkUrl(url, apiKey)
                checking = false
            }
        }
    }

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
                "Paste a link, or the whole message it came in. This checks for known scam wording patterns, and, if a Safe Browsing key is set in Settings, against Google's live malware and phishing database.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it; result = null; safeBrowsingResult = null },
                label = { Text("Link or message text") },
                placeholder = { Text("e.g. \"Your account is suspended, verify now at bit.ly/xyz\"") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { runCheck() },
                enabled = input.isNotBlank() && !checking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (checking) "Checking…" else "Check It")
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(20.dp))

                if (res.extractedUrl != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(res.extractedUrl, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                safeBrowsingResult?.let { sbResult ->
                    val (sbColor, sbLabel, sbDetail) = when (sbResult) {
                        is SafeBrowsingHelper.SafeBrowsingResult.Flagged -> Triple(
                            Color(0xFFC53030),
                            "Flagged by Google Safe Browsing",
                            sbResult.threatTypes.joinToString(", ") { SafeBrowsingHelper.describeThreatType(it) }
                        )
                        SafeBrowsingHelper.SafeBrowsingResult.Safe -> Triple(
                            Color(0xFF2F855A), "Not found in Google's threat database", null
                        )
                        SafeBrowsingHelper.SafeBrowsingResult.NotConfigured -> Triple(
                            Color(0xFFB7791F), "Add a free Safe Browsing API key in Settings for real malware/phishing detection", null
                        )
                        is SafeBrowsingHelper.SafeBrowsingResult.Error -> Triple(
                            Color(0xFFB7791F), sbResult.message, null
                        )
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = sbColor.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(sbLabel, color = sbColor, fontWeight = FontWeight.Bold)
                            sbDetail?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val (color, label) = when (res.riskLevel) {
                    LinkCheckHelper.RiskLevel.HIGH -> Color(0xFFC53030) to "High caution advised (wording patterns)"
                    LinkCheckHelper.RiskLevel.MEDIUM -> Color(0xFFB7791F) to "Some warning signs (wording patterns)"
                    LinkCheckHelper.RiskLevel.LOW -> Color(0xFF2F855A) to "No obvious wording red flags"
                }
                Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(label, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (res.warnings.isNotEmpty()) {
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
