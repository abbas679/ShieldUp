package com.tahirabbas.shieldup.utils

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
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
        return listOf(
            checkScreenLock(context),
            checkDeveloperOptions(context),
            checkUnknownSources(context),
            checkOpenWifi(context),
            checkRootHeuristic()
        )
    }

    private fun checkScreenLock(context: Context): SecurityCheckItem {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val secure = keyguardManager.isDeviceSecure
        return SecurityCheckItem(
            title = "Screen Lock",
            passed = secure,
            description = if (secure)
                "A PIN, pattern, or biometric lock is set."
            else
                "No screen lock is set — anyone who picks up your phone has full access. Set one in Settings > Security."
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
                "USB debugging is off — good, this is a common attack vector when left on."
            else
                "USB debugging is ON. Unless you're actively developing, turn this off in Settings > Developer Options."
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
                "Installing apps from outside the Play Store is blocked — good default."
            else
                "This app is allowed to install apps from unknown sources. Only keep this on if you specifically need it."
        )
    }

    private fun checkOpenWifi(context: Context): SecurityCheckItem {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isWifi) {
            return SecurityCheckItem("WiFi Security", true, "Not currently on WiFi — nothing to check right now.")
        }

        // Best-effort: Android doesn't expose current network encryption type directly
        // to normal apps without location permission (SSID/security details are
        // gated behind ACCESS_FINE_LOCATION for privacy reasons). We keep this
        // check honest rather than requesting a sensitive permission just for this.
        return SecurityCheckItem(
            title = "WiFi Security",
            passed = true,
            description = "Connected to WiFi. To check if it's password-protected, tap your network name in Android's WiFi settings — avoid entering passwords on open/public networks."
        )
    }

    private fun checkRootHeuristic(): SecurityCheckItem {
        val suspiciousPaths = listOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
            "/su/bin/su"
        )
        val found = suspiciousPaths.any { File(it).exists() }
        val testKeys = Build.TAGS?.contains("test-keys") == true

        val flagged = found || testKeys
        return SecurityCheckItem(
            title = "Root Detection",
            passed = !flagged,
            description = if (!flagged)
                "No common root indicators found. (Best-effort check — not a guarantee.)"
            else
                "Some signs of a rooted/modified device were found. If you didn't root this device intentionally, that's a serious concern. (Best-effort heuristic — false positives are possible.)"
        )
    }
}
