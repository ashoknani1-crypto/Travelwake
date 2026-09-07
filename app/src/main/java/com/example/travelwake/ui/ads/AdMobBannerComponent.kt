package com.example.travelwake.ui.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassNavyBorder
import com.example.ui.theme.GlassNavyCard
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

/**
 * Platform manager for Google Mobile Ads SDK initialization.
 */
object AdMobPlatformManager {
    private const val TAG = "AdMobPlatformManager"
    private var isInitialized = false

    /**
     * Initializes Google Mobile Ads SDK on the platform thread.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "AdMob initialization status: $status")
                }
                isInitialized = true
            } catch (e: Exception) {
                Log.w(TAG, "AdMob initialization encountered error: ${e.message}")
            }
        }
    }
}

/**
 * Non-intrusive banner ad component using Google Mobile Ads SDK platform integration.
 * Styled with TravelWake translucent glass aesthetics and designed specifically
 * to be placed on non-critical screens (Home, Belongings, Settings) while never
 * overlapping or interfering with critical journey alarms or navigation controls.
 *
 * @param adUnitId Google AdMob banner ad unit ID (defaults to Google official test unit ID)
 * @param modifier Composable modifier for outer positioning
 */
@Composable
fun NonIntrusiveBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Official Google Test Banner Ad Unit ID
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    if (isInspection) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassNavyCard)
                .border(1.dp, GlassBorderHighlight, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sponsored Partner Preview",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Wrap banner in a clean, non-intrusive container matching glassmorphism aesthetics
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1A0A1128)) // Subtle translucent navy backing
            .border(0.5.dp, GlassNavyBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .testTag("non_intrusive_banner_ad"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d("BannerAd", "Non-intrusive banner ad loaded successfully.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            Log.w("BannerAd", "Banner ad failed to load (${error.code}): ${error.message}")
                        }
                    }
                    val request = AdRequest.Builder().build()
                    loadAd(request)
                }
            },
            update = { adView ->
                // Ensure ad remains responsive
            }
        )
    }
}
