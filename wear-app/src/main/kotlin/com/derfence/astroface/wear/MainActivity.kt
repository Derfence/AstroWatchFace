package com.derfence.astroface.wear

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.derfence.astroface.wear.complication.DialUpdateRequester
import com.derfence.astroface.wear.store.AndroidInstalledPackageLookup
import com.derfence.astroface.wear.store.PlayStoreNavigator
import com.derfence.astroface.wear.store.WatchFaceAvailability
import com.derfence.astroface.wear.status.RequestStatusRepository

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var installWatchFaceButton: Button
    private val watchFaceAvailability by lazy {
        WatchFaceAvailability(AndroidInstalledPackageLookup(packageManager))
    }
    private val playStoreNavigator by lazy {
        PlayStoreNavigator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusView = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 13f
            setLineSpacing(2f, 1f)
        }

        val refreshButton = Button(this).apply {
            text = "Rafraîchir les éléments"
            setOnClickListener {
                DialUpdateRequester.requestAll(this@MainActivity)
                refreshStatus()
                Toast.makeText(
                    this@MainActivity,
                    "Demande envoyée aux éléments",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        installWatchFaceButton = Button(this).apply {
            text = "Installer la watch face"
            setOnClickListener {
                if (!playStoreNavigator.openWatchFaceStore()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Impossible d'ouvrir la fiche Play Store",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.BLACK)
            addView(TextView(this@MainActivity).apply {
                text = "AstroFace Data"
                setTextColor(Color.WHITE)
                textSize = 18f
                gravity = Gravity.CENTER
            })
            addView(statusView)
            addView(refreshButton)
            addView(installWatchFaceButton)
        }

        setContentView(ScrollView(this).apply { addView(content) })
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val status = RequestStatusRepository.read(this)
        val isWatchFaceInstalled = watchFaceAvailability.isWatchFaceInstalled()
        installWatchFaceButton.visibility = if (isWatchFaceInstalled) View.GONE else View.VISIBLE
        statusView.text = buildString {
            appendLine()
            appendLine(
                "Watch face AstroFace : " +
                    if (isWatchFaceInstalled) "installée" else "absente"
            )
            appendLine()
            appendLine("Service 24 h : prêt")
            appendLine("Service positions célestes : prêt")
            appendLine("Service statut central : prêt")
            appendLine("Service aiguille 24 h : prêt")
            appendLine()
            appendLine("Dernière requête 24 h :")
            appendLine(status.last24h ?: "aucune")
            appendLine()
            appendLine("Dernière requête positions célestes :")
            appendLine(status.lastCelestialOverlay ?: "aucune")
            appendLine()
            appendLine("Dernière requête statut central :")
            appendLine(status.lastStatusOverlay ?: "aucune")
            appendLine()
            appendLine("Dernière requête aiguille 24 h :")
            appendLine(status.last24hHand ?: "aucune")
            appendLine()
            appendLine("Dernier rafraîchissement manuel :")
            appendLine(status.lastManualRefresh ?: "aucun")
        }
    }
}
