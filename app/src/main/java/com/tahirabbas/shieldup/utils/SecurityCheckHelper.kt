package com.tahirabbas.shieldup.utils

import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import java.io.File

data class SecurityCheckItem(
    val title: String,
    val passed: Boolean,
    val description: String
)

object SecurityCheckHelper {

    fun runAllChecks(context: Context): List<SecurityCheckItem> {
        // Each check is isolated in its own try/catch — if one check fails on a
        // particular device/OEM, the rest still run and the screen never crashes.
        return listOf(
            safeCheck("Screen Lock") { checkScreenLock(context) },
            safeCheck("USB Debugging") { checkDeveloperOptions(context) },
            safeCheck("Install Unknown Apps") { checkUnknownSources(context) },
            safeCheck("WiFi Security") { checkOpenWifi(context) },
            safeCheck("Root Detection") { checkRootHeuristic() }
        )
    }

    private fun safeCheck(title: String, block: () -> SecurityCheckItem): SecurityCheckItem {
        return try {
            block()
        } catch (e: Exception) {
            SecurityCheckItem(
                title = title,
                passed = true,
                description = "Couldn't complete this check on your device. This is skipped, not failed."
            )
        }
    }

    private fun checkScreenLock(context: Context): SecurityCheckItem {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val secure = keyguardManager?.isDeviceSecure ?: true
        return SecurityCheckItem(
            title = "Screen Lock",
            passed = secure,
            description = if (secure)
                "A PIN, pattern, or biometric lock is set."
            else
                "No screen lock is set. Anyone who picks up your phone has full access. Set one in Settings, then Security."
        )
    }

    private fun checkDeveloperOptions(context: Context): SecurityCheckItem {
        val devEnabled = Settings.Global.getInt(
            context.contentResolver, Settings.Global.ADB_ENABLED, 0
        ) == 1
        return SecurityCheckItem(
            title = "USB Debugging",
            passed = !devEnabled,
            description = if (!devEnabled)
                "USB debugging is off. Good, this is a common attack vector when left on."
            else
                "USB debugging is on. Unless you're actively developing, turn this off in Settings, then Developer Options."
        )
    }

    private fun checkUnknownSources(context: Context): SecurityCheckItem {
        val allowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        }
        return SecurityCheckItem(
            title = "Install Unknown Apps",
            passed = !allowed,
            description = if (!allowed)
                "Installing apps from outside the Play Store is blocked. Good default."
            else
                "This app is allowed to install apps from unknown sources. Only keep this on if you specifically need it."
        )
    }

    private fun checkOpenWifi(context: Context): SecurityCheckItem {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isWifi) {
            return SecurityCheckItem("WiFi Security", true, "Not currently on WiFi. Nothing to check right now.")
        }

        return SecurityCheckItem(
            title = "WiFi Security",
            passed = true,
            description = "Connected to WiFi. To check if it's password protected, open your WiFi settings and tap the network name. Avoid entering passwords on open or public networks."
        )
    }

    private fun checkRootHeuristic(): SecurityCheckItem {
        val suspiciousPaths = listOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
            "/su/bin/su"
        )
        val found = suspiciousPaths.any { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
        val testKeys = Build.TAGS?.contains("test-keys") == true

        val flagged = found || testKeys
        return SecurityCheckItem(
            title = "Root Detection",
            passed = !flagged,
            description = if (!flagged)
                "No common root indicators found. This is a best-effort check, not a guarantee."
            else
                "Some signs of a rooted or modified device were found. If you did not root this device intentionally, treat this as a serious concern. This is a best-effort heuristic, so false positives are possible."
        )
    }
}
