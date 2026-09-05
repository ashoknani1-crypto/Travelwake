package com.example.travelwake.ui.map

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.example.travelwake.engine.LocationEngine
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Interactive, high-fidelity MapView visualizing the user's route, current position,
 * destination marker, and alert radius geofence zone during the active trip alarm service.
 */
class RouteMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Coordinates
    private var userLatitude: Double = 18.5204
    private var userLongitude: Double = 73.8567
    private var destLatitude: Double = 18.5289
    private var destLongitude: Double = 73.8744
    private var destName: String = "Destination"
    private var alertDistanceMeters: Int = 500
    private var remainingDistanceMeters: Int = 2400

    // Visual theme colors (customizable via Weather Theme)
    var primaryColor: Int = Color.parseColor("#005AC1")
    var accentColor: Int = Color.parseColor("#009688")
    var mapBackgroundColor: Int = Color.parseColor("#F3F4F9")
    var roadColor: Int = Color.parseColor("#E0E2EC")
    var gridColor: Int = Color.parseColor("#D0D3DE")

    // Camera / Pan & Zoom
    private var zoomScale: Float = 1.0f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    // Pulse animation for user GPS position
    private var pulseRadiusFraction: Float = 0f
    private val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1800
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            pulseRadiusFraction = it.animatedValue as Float
            invalidate()
        }
    }

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val routeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val userPinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val userPulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val destPinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val alertGeofencePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(16f, 12f), 0f)
    }
    private val alertGeofenceFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1C1E")
        textSize = 34f
        isFakeBoldText = true
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44474E")
        textSize = 26f
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        setShadowLayer(8f, 0f, 4f, Color.parseColor("#20000000"))
    }

    // Gestures
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // Offline Map Tile Engine
    val offlineTileEngine = OfflineMapTileEngine(context)

    init {
        pulseAnimator.start()

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                zoomScale *= detector.scaleFactor
                zoomScale = max(0.6f, min(zoomScale, 3.5f))
                invalidate()
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                offsetX -= distanceX
                offsetY -= distanceY
                invalidate()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Reset camera focus to fit route
                zoomScale = 1.0f
                offsetX = 0f
                offsetY = 0f
                invalidate()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled
        return handled || super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator.cancel()
    }

    fun updateCoordinates(
        userLat: Double,
        userLon: Double,
        destLat: Double,
        destLon: Double,
        destinationName: String,
        alertBufferMeters: Int,
        distanceMeters: Int
    ) {
        userLatitude = userLat
        userLongitude = userLon
        destLatitude = destLat
        destLongitude = destLon
        destName = destinationName
        alertDistanceMeters = alertBufferMeters
        remainingDistanceMeters = distanceMeters
        offlineTileEngine.preCacheRouteTiles(
            startLat = userLatitude,
            startLon = userLongitude,
            destLat = destLatitude,
            destLon = destLongitude,
            alertBufferMeters = alertDistanceMeters
        )
        invalidate()
    }

    fun toggleWeakSignalSimulation(): Boolean {
        val result = offlineTileEngine.toggleWeakSignalSimulation()
        invalidate()
        return result
    }

    fun zoomIn() {
        zoomScale = min(zoomScale * 1.25f, 3.5f)
        invalidate()
    }

    fun zoomOut() {
        zoomScale = max(zoomScale * 0.8f, 0.6f)
        invalidate()
    }

    fun centerOnUser() {
        zoomScale = 1.2f
        offsetX = 0f
        offsetY = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // 1. Draw Map Canvas Background
        backgroundPaint.color = mapBackgroundColor
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)

        // 1.5. Draw Offline Map Vector & Terrain Tiles (Ensures route visibility without cell signal)
        offlineTileEngine.drawOfflineTiles(canvas, w, h, zoomScale, offsetX, offsetY, primaryColor)

        // 2. Draw Decorative Map Grid & Transit Lines
        gridPaint.color = gridColor
        val gridSize = 80f * zoomScale
        var gx = (offsetX % gridSize)
        while (gx < w) {
            canvas.drawLine(gx, 0f, gx, h, gridPaint)
            gx += gridSize
        }
        var gy = (offsetY % gridSize)
        while (gy < h) {
            canvas.drawLine(0f, gy, w, gy, gridPaint)
            gy += gridSize
        }

        // Secondary background road networks
        roadPaint.color = roadColor
        roadPaint.strokeWidth = 14f * zoomScale
        val roadPath = Path().apply {
            moveTo(0f, h * 0.7f + offsetY * 0.3f)
            cubicTo(w * 0.3f, h * 0.6f + offsetY * 0.3f, w * 0.6f, h * 0.8f + offsetY * 0.3f, w, h * 0.65f + offsetY * 0.3f)
            moveTo(w * 0.2f, 0f)
            lineTo(w * 0.25f, h)
        }
        canvas.drawPath(roadPath, roadPaint)

        // 3. Compute Map Coordinates to Canvas Screen Positions
        // Map user position near bottom-left and destination near top-right with margin
        val centerX = w * 0.5f + offsetX
        val centerY = h * 0.5f + offsetY

        // Relative delta vector
        val deltaLat = (destLatitude - userLatitude)
        val deltaLon = (destLongitude - userLongitude)
        val distTotal = max(LocationEngine.calculateDistanceMeters(userLatitude, userLongitude, destLatitude, destLongitude), 100)

        // Screen positions based on projection
        val span = min(w, h) * 0.55f * zoomScale
        val userScreenX = centerX - span * 0.45f
        val userScreenY = centerY + span * 0.45f

        val destScreenX = centerX + span * 0.45f
        val destScreenY = centerY - span * 0.45f

        // 4. Draw Alert Geofence Perimeter around Destination
        val geofenceRadius = max(span * 0.35f * (alertDistanceMeters.toFloat() / max(distTotal.toFloat(), 500f)), 50f)
        val isInsideAlertRange = remainingDistanceMeters <= alertDistanceMeters

        alertGeofenceFillPaint.color = if (isInsideAlertRange) Color.parseColor("#28BA1A1A") else Color.parseColor("#15005AC1")
        canvas.drawCircle(destScreenX, destScreenY, geofenceRadius, alertGeofenceFillPaint)

        alertGeofencePaint.color = if (isInsideAlertRange) Color.parseColor("#BA1A1A") else primaryColor
        canvas.drawCircle(destScreenX, destScreenY, geofenceRadius, alertGeofencePaint)

        // 5. Draw Dynamic Route Polyline with Curved Waypoints
        val routePath = Path().apply {
            moveTo(userScreenX, userScreenY)
            val midX1 = userScreenX + (destScreenX - userScreenX) * 0.35f + 40f * zoomScale
            val midY1 = userScreenY + (destScreenY - userScreenY) * 0.25f - 30f * zoomScale
            val midX2 = userScreenX + (destScreenX - userScreenX) * 0.7f - 30f * zoomScale
            val midY2 = userScreenY + (destScreenY - userScreenY) * 0.75f + 20f * zoomScale
            cubicTo(midX1, midY1, midX2, midY2, destScreenX, destScreenY)
        }

        // Route Glow
        routeGlowPaint.color = primaryColor
        routeGlowPaint.alpha = 50
        routeGlowPaint.strokeWidth = 24f * zoomScale
        canvas.drawPath(routePath, routeGlowPaint)

        // Route Primary Line
        routePaint.color = primaryColor
        routePaint.strokeWidth = 10f * zoomScale
        canvas.drawPath(routePath, routePaint)

        // 6. Draw Distance Remaining Floating Badge along the Route
        val routeMidX = (userScreenX + destScreenX) * 0.5f
        val routeMidY = (userScreenY + destScreenY) * 0.5f - 20f * zoomScale
        val distText = LocationEngine.formatDistance(remainingDistanceMeters) + " to stop"
        val badgeWidth = textPaint.measureText(distText) + 40f
        val badgeRect = RectF(routeMidX - badgeWidth / 2f, routeMidY - 32f, routeMidX + badgeWidth / 2f, routeMidY + 32f)

        badgePaint.color = Color.WHITE
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgePaint)
        textPaint.color = primaryColor
        textPaint.textSize = 28f
        canvas.drawText(distText, routeMidX - textPaint.measureText(distText) / 2f, routeMidY + 10f, textPaint)

        // 7. Draw Destination Marker
        destPinPaint.color = Color.parseColor("#BA1A1A")
        canvas.drawCircle(destScreenX, destScreenY, 18f * zoomScale, destPinPaint)
        destPinPaint.color = Color.WHITE
        canvas.drawCircle(destScreenX, destScreenY, 8f * zoomScale, destPinPaint)

        // Destination Callout Card
        val destTitle = "⚑ $destName"
        val destWidth = textPaint.measureText(destTitle) + 40f
        val destCardRect = RectF(destScreenX - destWidth / 2f, destScreenY - 75f * zoomScale, destScreenX + destWidth / 2f, destScreenY - 25f * zoomScale)
        badgePaint.color = Color.parseColor("#1A1C1E")
        canvas.drawRoundRect(destCardRect, 16f, 16f, badgePaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 26f
        canvas.drawText(destTitle, destScreenX - textPaint.measureText(destTitle) / 2f, destScreenY - 42f * zoomScale, textPaint)

        // 8. Draw User Current GPS Location Pin (Animated Pulse)
        val pulseRadius = (20f + 40f * pulseRadiusFraction) * zoomScale
        userPulsePaint.color = primaryColor
        userPulsePaint.strokeWidth = 4f
        userPulsePaint.alpha = ((1f - pulseRadiusFraction) * 200).toInt()
        canvas.drawCircle(userScreenX, userScreenY, pulseRadius, userPulsePaint)

        // Accuracy Halo
        userPinPaint.color = Color.parseColor("#4D005AC1")
        canvas.drawCircle(userScreenX, userScreenY, 26f * zoomScale, userPinPaint)

        // Center Blue Dot
        userPinPaint.color = Color.WHITE
        canvas.drawCircle(userScreenX, userScreenY, 16f * zoomScale, userPinPaint)
        userPinPaint.color = primaryColor
        canvas.drawCircle(userScreenX, userScreenY, 11f * zoomScale, userPinPaint)

        // User Label Callout
        val userLabel = "● My Position (GPS Live)"
        val userWidth = subTextPaint.measureText(userLabel) + 32f
        val userRect = RectF(userScreenX - userWidth / 2f, userScreenY + 24f * zoomScale, userScreenX + userWidth / 2f, userScreenY + 66f * zoomScale)
        badgePaint.color = Color.WHITE
        canvas.drawRoundRect(userRect, 14f, 14f, badgePaint)
        subTextPaint.color = primaryColor
        subTextPaint.textSize = 22f
        canvas.drawText(userLabel, userScreenX - subTextPaint.measureText(userLabel) / 2f, userScreenY + 50f * zoomScale, subTextPaint)

        // 9. Map HUD Controls & Legend (Top Overlay)
        drawHudOverlay(canvas, w, h, isInsideAlertRange)
    }

    private fun drawHudOverlay(canvas: Canvas, w: Float, h: Float, isAlerting: Boolean) {
        val stats = offlineTileEngine.cacheStats.value
        val isWeakSignal = stats.simulatedWeakSignal

        // Top status pill
        val statusRect = RectF(20f, 20f, w - 20f, 80f)
        badgePaint.color = if (isWeakSignal) Color.parseColor("#FFF4E5") else Color.parseColor("#F5FFFFFF")
        canvas.drawRoundRect(statusRect, 20f, 20f, badgePaint)

        textPaint.color = when {
            isAlerting -> Color.parseColor("#BA1A1A")
            isWeakSignal -> Color.parseColor("#B25E00")
            else -> primaryColor
        }
        textPaint.textSize = 26f
        val statusText = when {
            isAlerting -> "🚨 WITHIN ALARM ZONE (${alertDistanceMeters}m)"
            isWeakSignal -> "📡 OFFLINE TILES ACTIVE (${stats.totalTiles} Cached) • Cell Weak"
            else -> "📍 ROUTE ACTIVE • ${stats.totalTiles} Offline Tiles Ready"
        }
        canvas.drawText(statusText, 36f, 58f, textPaint)

        // Legend at bottom
        val legendRect = RectF(20f, h - 70f, w - 20f, h - 20f)
        badgePaint.color = Color.parseColor("#F5FFFFFF")
        canvas.drawRoundRect(legendRect, 16f, 16f, badgePaint)

        subTextPaint.textSize = 21f
        subTextPaint.color = Color.parseColor("#44474E")
        val legendText = if (isWeakSignal) {
            "🗺️ Offline Cache (${stats.totalTiles} tiles)  |  ⭕ Geofence (${alertDistanceMeters}m)"
        } else {
            "● GPS  |  ⚑ Stop  |  ⭕ Geofence (${alertDistanceMeters}m)  |  💾 Offline Ready"
        }
        canvas.drawText(legendText, 32f, h - 38f, subTextPaint)
    }
}
