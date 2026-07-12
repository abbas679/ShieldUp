package com.tahirabbas.shieldup.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.KnownDeviceRepository
import com.tahirabbas.shieldup.utils.LanDevice
import com.tahirabbas.shieldup.utils.WifiScanHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerScreen(
    deviceRepository: KnownDeviceRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf<List<LanDevice>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var labelingDevice by remember { mutableStateOf<LanDevice?>(null) }
    var labelInput by remember { mutableStateOf("") }
    val gatewayIp = remember { WifiScanHelper.getGatewayIp(context) }

    fun startScan() {
        scanning = true
        scope.launch {
            devices = WifiScanHelper.scanForDevices(context)
            scanning = false
            scanned = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home WiFi Scan") },
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
                "Scans your own WiFi network for connected devices. This takes about " +
                        "10-20 seconds. Make sure you're connected to WiFi first.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(onClick = { startScan() }, enabled = !scanning, modifier = Modifier.fillMaxWidth()) {
                Text(if (scanning) "Scanning…" else "Scan Network")
            }

            if (scanning) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (scanned && !scanning) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("${devices.size} device(s) found", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(devices) { device ->
                        val known = deviceRepository.isKnown(device.macAddress)
                        val label = deviceRepository.labelFor(device.macAddress)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = {
                                labelingDevice = device
                                labelInput = label ?: ""
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (known) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (known) Color(0xFF2F855A) else Color(0xFFB7791F)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label ?: "Unrecognized device", fontWeight = FontWeight.SemiBold)
                                    Text(device.ipAddress, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    Text(device.macAddress, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            if (gatewayIp != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://$gatewayIp")))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Open Router Admin Page", fontWeight = FontWeight.SemiBold)
                            Text(
                                "$gatewayIp — to block devices or change your WiFi password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    labelingDevice?.let { device ->
        AlertDialog(
            onDismissRequest = { labelingDevice = null },
            title = { Text("Label this device") },
            text = {
                Column {
                    Text(device.ipAddress, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("e.g. Dad's phone, Smart TV") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (labelInput.isNotBlank()) {
                        deviceRepository.labelDevice(device.macAddress, labelInput.trim())
                    }
                    labelingDevice = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { labelingDevice = null }) { Text("Cancel") }
            }
        )
    }
}
