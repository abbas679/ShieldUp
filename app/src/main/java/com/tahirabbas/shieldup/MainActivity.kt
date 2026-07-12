package com.tahirabbas.shieldup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tahirabbas.shieldup.data.CallLogRepository
import com.tahirabbas.shieldup.data.CodewordRepository
import com.tahirabbas.shieldup.data.FamilyContactRepository
import com.tahirabbas.shieldup.data.KnownDeviceRepository
import com.tahirabbas.shieldup.navigation.ShieldUpNavGraph
import com.tahirabbas.shieldup.ui.theme.ShieldUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codewordRepository = CodewordRepository(applicationContext)
        val contactRepository = FamilyContactRepository(applicationContext)
        val logRepository = CallLogRepository(applicationContext)
        val deviceRepository = KnownDeviceRepository(applicationContext)

        setContent {
            ShieldUpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShieldUpNavGraph(
                        codewordRepository = codewordRepository,
                        contactRepository = contactRepository,
                        logRepository = logRepository,
                        deviceRepository = deviceRepository
                    )
                }
            }
        }
    }
}
