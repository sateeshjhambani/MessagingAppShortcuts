package com.example.messagingappshortcuts

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.messagingappshortcuts.ui.theme.MessagingAppShortcutsTheme

class MainActivity: ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            MessagingAppShortcutsTheme {
                Column(
                    modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        16.dp, Alignment.CenterVertically
                    )
                ) {
                    val labelText = when (viewModel.shortcutType) {
                        ShortcutType.STATIC -> "Static Shortcut Clicked"
                        ShortcutType.DYNAMIC -> "Dynamic Shortcut Clicked"
                        ShortcutType.PINNED -> "Pinned Shortcut Clicked"
                        null -> ""
                    }
                    Text(labelText)
                    Button(
                        onClick = { addDynamicShortcut() }
                    ) {
                        Text(text = "Add dynamic shortcut")
                    }
                    Button(
                        onClick = { addPinnedShortcut() }
                    ) {
                        Text(text = "Add pinned shortcut")
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun addPinnedShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val shortcutManager = getSystemService<ShortcutManager>()!!
        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcut = ShortcutInfo.Builder(applicationContext, ShortcutType.PINNED.name)
                .setShortLabel("Send Message")
                .setLongLabel("This sends a message to a friend")
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.baseline_baby_changing_station_24))
                .setIntent(
                    Intent(applicationContext, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra(SHORTCUT_ID_KEY, ShortcutType.PINNED.name)
                    }
                )
                .build()

            val callbackIntent = shortcutManager.createShortcutResultIntent(shortcut)
            val successPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                callbackIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            shortcutManager.requestPinShortcut(shortcut, successPendingIntent.intentSender)
        }
    }

    private fun addDynamicShortcut() {
        val shortcut = ShortcutInfoCompat.Builder(applicationContext, "messaging").setShortLabel("Call Mom")
            .setLongLabel("Clicking this will call your mom")
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext, R.drawable.baseline_baby_changing_station_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra(SHORTCUT_ID_KEY, ShortcutType.DYNAMIC.name)
                }
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }

    private fun handleIntent(intent: Intent?) = intent?.let {
        when (it.getStringExtra(SHORTCUT_ID_KEY)) {
            ShortcutType.DYNAMIC.name -> viewModel.onShortcutClicked(ShortcutType.DYNAMIC)
            ShortcutType.STATIC.name -> viewModel.onShortcutClicked(ShortcutType.STATIC)
            ShortcutType.PINNED.name -> viewModel.onShortcutClicked(ShortcutType.PINNED)
        }
    }

    companion object {

        const val SHORTCUT_ID_KEY = "shortcut_id"
    }
}
