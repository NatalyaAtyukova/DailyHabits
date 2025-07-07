package com.habittracker.dailyhabits

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader

class SplashActivity : ComponentActivity() {
    private val adUnitId = "R-M-14492374-3"
    private var appOpenAd: AppOpenAd? = null
    private var adLoader: AppOpenAdLoader? = null
    private val TAG = "SplashActivity"
    private var isAdShownOrFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()

        // Таймаут: если реклама не загрузилась за 3 секунды — идём дальше
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdShownOrFailed) {
                isAdShownOrFailed = true
                goToMain()
            }
        }, 3000)
    }

    private fun loadAd() {
        adLoader = AppOpenAdLoader(this).apply {
            setAdLoadListener(object : AppOpenAdLoadListener {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenAd?.setAdEventListener(createAdEventListener())
                    showAd()
                }
                override fun onAdFailedToLoad(error: com.yandex.mobile.ads.common.AdRequestError) {
                    Log.e(TAG, "Failed to load splash ad: ${error.description}")
                    isAdShownOrFailed = true
                    goToMain()
                }
            })
        }
        val adRequestConfiguration = com.yandex.mobile.ads.common.AdRequestConfiguration.Builder(adUnitId).build()
        adLoader?.loadAd(adRequestConfiguration)
    }

    private fun showAd() {
        try {
            appOpenAd?.show(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing splash ad: ${e.message}", e)
            isAdShownOrFailed = true
            goToMain()
        }
    }

    private fun createAdEventListener(): AppOpenAdEventListener {
        return object : AppOpenAdEventListener {
            override fun onAdShown() {
                Log.d(TAG, "Splash ad shown")
            }
            override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {
                Log.e(TAG, "Failed to show splash ad: ${error.description}")
                isAdShownOrFailed = true
                goToMain()
            }
            override fun onAdDismissed() {
                Log.d(TAG, "Splash ad dismissed")
                isAdShownOrFailed = true
                goToMain()
            }
            override fun onAdClicked() {
                Log.d(TAG, "Splash ad clicked")
            }
            override fun onAdImpression(impressionData: com.yandex.mobile.ads.common.ImpressionData?) {
                Log.d(TAG, "Splash ad impression recorded")
            }
        }
    }

    private fun goToMain() {
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        appOpenAd?.setAdEventListener(null)
        adLoader = null
        appOpenAd = null
        super.onDestroy()
    }
} 