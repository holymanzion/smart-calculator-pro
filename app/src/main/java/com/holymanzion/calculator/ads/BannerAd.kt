package com.holymanzion.calculator.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.holymanzion.calculator.BuildConfig

/** Whether ads may be requested at all: false until consent resolves, or if ads are off. */
val LocalAdsEnabled = staticCompositionLocalOf { false }

/**
 * An adaptive banner.
 *
 * Deliberately occupies no space until an ad actually loads. A reserved-but-empty slot
 * is the worst of both worlds — it costs layout on every screen while showing nothing
 * when there is no fill, which is common outside the large ad markets.
 *
 * Anchored adaptive sizing asks for a height suited to the device rather than the old
 * fixed 320x50, so it neither looks cramped on a tablet nor overwhelms a small phone.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    if (!LocalAdsEnabled.current) return

    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    var loaded by remember { mutableStateOf(false) }

    val adView = remember(widthDp) {
        AdView(context).apply {
            setAdSize(adaptiveSize(context, widthDp))
            adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    loaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    // No fill is routine, not an error worth surfacing. Stay collapsed.
                    loaded = false
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    if (!loaded) return

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth(),
    )
}

private fun adaptiveSize(context: Context, widthDp: Int): AdSize =
    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)

/** Convenience for screens that provide the flag from app state. */
@Composable
fun ProvideAdsEnabled(enabled: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAdsEnabled provides enabled, content = content)
}
