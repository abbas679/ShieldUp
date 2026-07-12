package com.tahirabbas.shieldup.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tahirabbas.shieldup.data.CallLogRepository
import com.tahirabbas.shieldup.data.CodewordRepository
import com.tahirabbas.shieldup.data.FamilyContactRepository
import com.tahirabbas.shieldup.data.KnownDeviceRepository
import com.tahirabbas.shieldup.ui.screens.CallLogScreen
import com.tahirabbas.shieldup.ui.screens.ClipboardCheckScreen
import com.tahirabbas.shieldup.ui.screens.CodewordSetupScreen
import com.tahirabbas.shieldup.ui.screens.FamilyContactsScreen
import com.tahirabbas.shieldup.ui.screens.HomeScreen
import com.tahirabbas.shieldup.ui.screens.LinkCheckerScreen
import com.tahirabbas.shieldup.ui.screens.SecurityCheckupScreen
import com.tahirabbas.shieldup.ui.screens.SettingsScreen
import com.tahirabbas.shieldup.ui.screens.VerifyCallScreen
import com.tahirabbas.shieldup.ui.screens.WifiScannerScreen

private object Routes {
    const val HOME = "home"
    const val VERIFY_CALL = "verify_call"
    const val CONTACTS = "contacts"
    const val CODEWORD = "codeword"
    const val CALL_LOG = "call_log"
    const val LINK_CHECKER = "link_checker"
    const val SECURITY_CHECKUP = "security_checkup"
    const val CLIPBOARD_CHECK = "clipboard_check"
    const val WIFI_SCANNER = "wifi_scanner"
    const val SETTINGS = "settings"
}

@Composable
fun ShieldUpNavGraph(
    codewordRepository: CodewordRepository,
    contactRepository: FamilyContactRepository,
    logRepository: CallLogRepository,
    deviceRepository: KnownDeviceRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onVerifyCall = { navController.navigate(Routes.VERIFY_CALL) },
                onLinkChecker = { navController.navigate(Routes.LINK_CHECKER) },
                onSecurityCheckup = { navController.navigate(Routes.SECURITY_CHECKUP) },
                onClipboardCheck = { navController.navigate(Routes.CLIPBOARD_CHECK) },
                onWifiScanner = { navController.navigate(Routes.WIFI_SCANNER) },
                onCallLog = { navController.navigate(Routes.CALL_LOG) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.VERIFY_CALL) {
            VerifyCallScreen(
                logRepository = logRepository,
                onBack = { navController.popBackStack() },
                onSaved = { navController.navigate(Routes.CALL_LOG) { popUpTo(Routes.HOME) } }
            )
        }

        composable(Routes.CONTACTS) {
            FamilyContactsScreen(
                contactRepository = contactRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CODEWORD) {
            CodewordSetupScreen(
                codewordRepository = codewordRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CALL_LOG) {
            CallLogScreen(
                logRepository = logRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LINK_CHECKER) {
            LinkCheckerScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SECURITY_CHECKUP) {
            SecurityCheckupScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CLIPBOARD_CHECK) {
            ClipboardCheckScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.WIFI_SCANNER) {
            WifiScannerScreen(
                deviceRepository = deviceRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenCodeword = { navController.navigate(Routes.CODEWORD) },
                onOpenContacts = { navController.navigate(Routes.CONTACTS) }
            )
        }
    }
}
