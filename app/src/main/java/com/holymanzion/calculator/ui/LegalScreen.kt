package com.holymanzion.calculator.ui

import android.graphics.Color as AndroidColor
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** The bundled legal documents, kept in sync with the copies published on the web. */
enum class LegalDocument(val title: String, val assetFile: String) {
    PrivacyPolicy("Privacy Policy", "privacy_policy.html"),
    Terms("Terms & Conditions", "terms.html"),
}

/**
 * Renders a bundled legal document.
 *
 * The HTML in `assets/` is the same file that is published to GitHub Pages, so the
 * in-app text and the public URL can never drift apart. JavaScript stays disabled —
 * these are static documents and there is no reason to widen the attack surface.
 *
 * Dark mode comes from the stylesheet's `prefers-color-scheme` rules, which WebView
 * honours from Android 13 onward; older versions simply show the light styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    document: LegalDocument,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    loadUrl("file:///android_asset/${document.assetFile}")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
