package com.habittracker.dailyhabits.ui.components

import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "R-M-14492374-2"
) {
    val context = LocalContext.current
    
    val bannerAd = remember {
        BannerAdView(context).apply {
            setAdUnitId(adUnitId)
            setAdSize(BannerAdSize.fixedSize(context, 320, 50))
            visibility = View.VISIBLE
            
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    Log.d("YandexAds", "Banner ad loaded")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.e("YandexAds", "Banner ad failed to load: ${error.description}")
                }

                override fun onAdClicked() {
                    Log.d("YandexAds", "Banner ad clicked")
                }

                override fun onLeftApplication() {
                    Log.d("YandexAds", "Left application")
                }

                override fun onReturnedToApplication() {
                    Log.d("YandexAds", "Returned to application")
                }

                override fun onImpression(impressionData: ImpressionData?) {
                    Log.d("YandexAds", "Banner impression recorded")
                }
            })
        }
    }
    
    DisposableEffect(key1 = bannerAd) {
        onDispose {
            bannerAd.destroy()
        }
    }
    
    AndroidView(
        factory = { bannerAd },
        modifier = modifier,
        update = { adView ->
            try {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            } catch (e: Exception) {
                Log.e("YandexAds", "Error loading ad: ${e.message}", e)
            }
        }
    )
} 