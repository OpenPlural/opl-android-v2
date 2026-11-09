package cc.webbiii.openplural.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.function.Function
import java.util.function.Predicate

class MainActivity : AppCompatActivity() {
    private var permRequestCode = 100
    private var permRequestDone = 100

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val notificationManager = getSystemService<NotificationManager>()!!
        notificationManager.createNotificationChannel(NotificationChannel(
            CHANNEL_CURRENT_FRONT,
            "Current Front",
            NotificationManager.IMPORTANCE_DEFAULT
        ))
        notificationManager.createNotificationChannel(NotificationChannel(
            CHANNEL_FRIEND_FRONT,
            "Friend Front",
            NotificationManager.IMPORTANCE_DEFAULT
        ))

        val permissionRequester = Function<String, Int> {
            val code = ++permRequestCode
            runOnUiThread {
                requestPermissions(arrayOf(it), code)
            }
            return@Function code
        }
        val permissionRequestChecker = Predicate<Int> {
            return@Predicate it >= permRequestDone
        }
        val bindings = WebBindings(this, notificationManager, permissionRequester, permissionRequestChecker)

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.allowContentAccess = false
        webView.settings.allowFileAccess = false
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.addJavascriptInterface(bindings, "OpenPluralApp")
        webView.loadUrl(WEB_URL)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permRequestDone = requestCode.coerceAtLeast(permRequestDone)
    }
}