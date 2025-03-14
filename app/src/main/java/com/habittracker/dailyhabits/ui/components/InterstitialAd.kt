package com.habittracker.dailyhabits.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

class InterstitialAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var adLoader: InterstitialAdLoader? = null
    private var onAdDismissedCallback: (() -> Unit)? = null
    private val adUnitId = "R-M-14492374-1"
    private val TAG = "InterstitialAdManager"

    init {
        adLoader = InterstitialAdLoader(context).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    interstitialAd?.setAdEventListener(createAdEventListener())
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.e(TAG, "Failed to load interstitial ad: ${error.description}")
                    interstitialAd = null
                    onAdDismissedCallback?.invoke()
                    onAdDismissedCallback = null
                }
            })
        }
        loadAd()
    }

    private fun createAdEventListener(): InterstitialAdEventListener {
        return object : InterstitialAdEventListener {
            override fun onAdShown() {
                Log.d(TAG, "Interstitial ad shown")
            }

            override fun onAdFailedToShow(error: AdError) {
                Log.e(TAG, "Failed to show interstitial ad: ${error.description}")
                onAdDismissedCallback?.invoke()
                onAdDismissedCallback = null
                loadAd()
            }

            override fun onAdDismissed() {
                Log.d(TAG, "Interstitial ad dismissed")
                onAdDismissedCallback?.invoke()
                onAdDismissedCallback = null
                loadAd()
            }

            override fun onAdClicked() {
                Log.d(TAG, "Interstitial ad clicked")
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d(TAG, "Interstitial ad impression recorded")
            }
        }
    }

    fun loadAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
        adLoader?.loadAd(adRequestConfiguration)
    }

    fun showAd(activity: Activity, onDismissed: () -> Unit) {
        onAdDismissedCallback = onDismissed
        if (interstitialAd != null) {
            try {
                interstitialAd?.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Error showing interstitial ad: ${e.message}", e)
                onAdDismissedCallback?.invoke()
                onAdDismissedCallback = null
                loadAd()
            }
        } else {
            Log.d(TAG, "Interstitial ad not loaded, proceeding with navigation")
            onAdDismissedCallback?.invoke()
            onAdDismissedCallback = null
            loadAd()
        }
    }

    fun destroy() {
        interstitialAd?.setAdEventListener(null)
        adLoader = null
        interstitialAd = null
        onAdDismissedCallback = null
    }
} 