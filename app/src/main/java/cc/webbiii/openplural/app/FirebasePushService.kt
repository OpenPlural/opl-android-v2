package cc.webbiii.openplural.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.NotificationResponse
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebasePushService : FirebaseMessagingService() {
    override fun onRegistered(installationId: String) {
        try {
            notificationId = installationId
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackgroundSyncReceived(message: RemoteMessage) {
        val notificationManager = getSystemService<NotificationManager>() ?: return
        val notification = makeNotification(message)

        val tag = message.notification?.tag
        if (tag != null) {
            notificationManager.notify(tag, COMMON_NOTIFICATION_ID, notification)
        } else {
            notificationManager.notify(makeUniqueCounter(), notification)
        }
    }

    override fun onNotificationReceived(message: RemoteMessage): NotificationResponse {
        val notification = makeNotification(message) ?: return NotificationResponse.suppress()

        val tag = message.notification?.tag
        return if (tag != null) {
            NotificationResponse.create(tag, COMMON_NOTIFICATION_ID, notification)
        } else {
            NotificationResponse.create(null, makeUniqueCounter(), notification)
        }
    }

    private fun makeNotification(message: RemoteMessage): Notification? {
        val notificationData = message.notification ?: return null

        return NotificationCompat.Builder(this, CHANNEL_FRIEND_FRONT)
            .setContentTitle(notificationData.title)
            .setContentText(notificationData.body)
            .setSmallIcon(R.drawable.ic_notify)
            .setAutoCancel(true)
            .build()
    }

    private fun makeUniqueCounter(): Int {
        return ((System.currentTimeMillis() / 1000L) - 1700000000L).toInt()
    }
}