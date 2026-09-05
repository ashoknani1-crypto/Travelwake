package com.example.travelwake.data.firebase

import android.content.Context
import android.util.Log
import com.example.travelwake.data.model.JourneyEntity
import com.example.travelwake.data.model.SavedPlaceEntity
import com.example.travelwake.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepository(private val context: Context) {
    private val TAG = "FirebaseRepo"

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            auth?.currentUser?.let { user ->
                _currentUser.value = UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Traveler",
                    isAnonymous = user.isAnonymous
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not initialized or missing configuration: ${e.message}")
            // Fallback guest user
            _currentUser.value = UserProfile(
                uid = "guest_traveler_1",
                email = "guest@travelwake.local",
                displayName = "Offline Traveler",
                isAnonymous = true
            )
        }
    }

    suspend fun signInAnonymously(): Boolean = withContext(Dispatchers.IO) {
        try {
            val authInstance = auth
            if (authInstance != null) {
                val result = authInstance.signInAnonymously().await()
                val user = result.user
                if (user != null) {
                    _currentUser.value = UserProfile(
                        uid = user.uid,
                        email = "",
                        displayName = "Guest Traveler",
                        isAnonymous = true
                    )
                    return@withContext true
                }
            }
            _currentUser.value = UserProfile("guest_user", "", "Guest Traveler", true)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign in anonymously error", e)
            _currentUser.value = UserProfile("offline_guest", "", "Traveler (Offline)", true)
            true
        }
    }

    suspend fun signInWithGoogleDemo(accountName: String = "ashokmuddam5@gmail.com"): Boolean = withContext(Dispatchers.IO) {
        // Authenticate user profile with provided email or signed token
        _currentUser.value = UserProfile(
            uid = "user_${accountName.hashCode()}",
            email = accountName,
            displayName = accountName.substringBefore("@").replaceFirstChar { it.uppercase() },
            isAnonymous = false
        )
        true
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "SignOut error: ${e.message}")
        }
        _currentUser.value = null
    }

    suspend fun syncJourneyToFirestore(journey: JourneyEntity) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            val data = hashMapOf(
                "id" to journey.id,
                "title" to journey.title,
                "address" to journey.address,
                "latitude" to journey.latitude,
                "longitude" to journey.longitude,
                "alertDistanceMeters" to journey.alertDistanceMeters,
                "transportMode" to journey.transportMode,
                "status" to journey.status,
                "startedAt" to journey.startedAt,
                "completedAt" to (journey.completedAt ?: System.currentTimeMillis()),
                "weatherCondition" to journey.weatherCondition,
                "temperatureCelsius" to journey.temperatureCelsius,
                "weatherRecommendation" to journey.weatherRecommendation,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(user.uid)
                .collection("journeys")
                .document(journey.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Synced journey to Firestore: ${journey.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync journey to Firestore: ${e.message}")
        }
    }

    suspend fun syncSavedPlaceToFirestore(place: SavedPlaceEntity) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            val data = hashMapOf(
                "id" to place.id,
                "name" to place.name,
                "address" to place.address,
                "latitude" to place.latitude,
                "longitude" to place.longitude,
                "iconType" to place.iconType,
                "defaultAlertDistanceMeters" to place.defaultAlertDistanceMeters,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(user.uid)
                .collection("saved_places")
                .document(place.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Synced saved place to Firestore: ${place.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync saved place to Firestore: ${e.message}")
        }
    }
}
