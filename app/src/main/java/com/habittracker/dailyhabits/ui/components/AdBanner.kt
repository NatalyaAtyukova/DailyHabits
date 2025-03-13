package com.habittracker.dailyhabits.ui.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "R-M-14492374-1"
) {
    val context = LocalContext.current
    
    val bannerAd = remember {
        BannerAdView(context).apply {
            setAdUnitId(adUnitId)
            setAdSize(BannerAdSize.fixedSize(context, 320, 50))
            visibility = View.VISIBLE
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
        update = {
            it.loadAd(AdRequest.Builder().build())
        }
    )
} 