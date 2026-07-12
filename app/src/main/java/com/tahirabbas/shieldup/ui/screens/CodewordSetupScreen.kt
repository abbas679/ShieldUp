package com.tahirabbas.shieldup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tahirabbas.shieldup.data.CodewordRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodewordSetupScreen(
    codewordRepository: CodewordRepository,
    onBack: () -> Unit
) {
    var codeword by remember { mutableStateOf(codewordRepository.getCodeword() ?: "") }
    var visible by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Codeword") },
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
                "Pick a word or short phrase only you and your trusted family know. " +
                        "Share it in person — never over text or a call, since that defeats the point.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = codeword,
                onValueChange = { codeword = it; saved = false },
                label = { Text("Codeword or phrase") },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (codeword.isNotBlank()) {
                        codewordRepository.setCodeword(codeword)
                        saved = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Codeword")
            }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Saved.", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Important", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "During a suspicious call, this app will NOT show you the codeword on screen — " +
                                "only a reminder to ask for it. That way, even if someone can see your " +
                                "phone screen during the call, the codeword itself is never exposed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
