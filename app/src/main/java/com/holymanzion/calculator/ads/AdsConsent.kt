package com.holymanzion.calculator.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.holymanzion.calculator.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GDPR/ePrivacy consent, via Google's User Messaging Platform.
 *
 * Required, not optional: in the EEA and UK, personalised ads without consent are
 * unlawful, and AdMob will simply refuse to fill requests from those regions if no
 * consent form has been shown.
 *
 * Outside those regions UMP reports consent as "not required" and the flow is a no-op,
 * so this costs users elsewhere nothing but a callback.
 */
object AdsConsent {

    /** The SDK must not be initialised twice, and both entry points can race. */
    private val initialised = AtomicBoolean(false)

    @Volatile
    private var consentInformation: ConsentInformation? = null

    /**
     * Gathers consent if needed, then initialises the ads SDK.
     *
     * Failure is deliberately quiet. If the consent form cannot be fetched — no
     * network, service down — the right outcome is an app with no ads, not an app
     * that will not start.
     */
    fun gatherConsentAndInitialise(activity: Activity, onReady: (canRequestAds: Boolean) -> Unit) {
        val parameters = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .apply {
                // Debug builds behave as if in the EEA, so the form can actually be
                // seen during development instead of silently skipping.
                if (BuildConfig.DEBUG) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .build(),
                    )
                }
            }
            .build()

        val information = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = information

        information.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    // The form's error is ignored on purpose: whatever happened, the
                    // only question that matters now is whether ads may be requested.
                    finish(activity, information.canRequestAds(), onReady)
                }
            },
            {
                // Could not even reach the consent service. Carry on without ads.
                finish(activity, information.canRequestAds(), onReady)
            },
        )
    }

    private fun finish(
        context: Context,
        canRequestAds: Boolean,
        onReady: (Boolean) -> Unit,
    ) {
        if (canRequestAds && initialised.compareAndSet(false, true)) {
            MobileAds.initialize(context.applicationContext)
        }
        onReady(canRequestAds)
    }

    /**
     * True when the user can be offered the privacy options form again — required so
     * consent is revocable, which is the part implementations usually forget.
     */
    fun privacyOptionsRequired(): Boolean =
        consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Reopens the consent form so a decision can be changed. */
    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }
}
