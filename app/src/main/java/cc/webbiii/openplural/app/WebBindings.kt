package cc.webbiii.openplural.app

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import java.util.function.Function
import java.util.function.Predicate

var notificationId: String? = null

class WebBindings(private val ctx: Context,
                  private val notificationManager: NotificationManager,
                  private val permissionRequester: Function<String, Int>,
                  private val permissionRequestChecker: Predicate<Int>) {
    @JavascriptInterface
    fun requestNotificationPermissions(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return permissionRequester.apply(Manifest.permission.POST_NOTIFICATIONS)
        }
        return -1
    }

    @JavascriptInterface
    fun isNotificationPermissionsRequestCompleted(code: Int): Boolean {
        return permissionRequestChecker.test(code)
    }

    @JavascriptInterface
    fun checkNotificationPermissions(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return "default"
        }
        return when (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> "granted"
            PackageManager.PERMISSION_DENIED -> "denied"
            else -> "default"
        }
    }

    @JavascriptInterface
    fun subscribeToNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            FirebaseMessaging.getInstance().register()
            return true
        }
        return false
    }

    @JavascriptInterface
    fun getNotificationSubscriptionId(): String? {
        return notificationId
    }

    @JavascriptInterface
    fun notifyCurrentFront(title: String, fronters: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notification = NotificationCompat.Builder(ctx, CHANNEL_CURRENT_FRONT)
            .setContentTitle(title)
            .setContentText(fronters)
            .setSmallIcon(R.drawable.ic_notify)
            .build()
        notificationManager.notify(COMMON_NOTIFICATION_ID, notification)
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        try {
            val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            return "${info.versionName} (v${info.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return "Error"
        }
    }

    @JavascriptInterface
    fun getBindingsVersion(): Int {
        return 1
    }

    @JavascriptInterface
    fun hasBindingsFeature(name: String): Boolean {
        return false
    }
}