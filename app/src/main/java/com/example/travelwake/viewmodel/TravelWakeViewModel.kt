package com.example.travelwake.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelwake.ai.GeminiPackingService
import com.example.travelwake.ai.GeminiPackingSuggestion
import com.example.travelwake.data.firebase.FirebaseRepository
import com.example.travelwake.data.gemini.GeminiRepository
import com.example.travelwake.data.local.AppDatabase
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.DestinationItem
import com.example.travelwake.data.model.JourneyEntity
import com.example.travelwake.data.model.JourneyStatus
import com.example.travelwake.data.model.PreAlarmStage
import com.example.travelwake.data.model.SavedPlaceEntity
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.data.model.TravelDestinationEntity
import com.example.travelwake.data.model.UserProfile
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.repository.BelongingsRepository
import com.example.travelwake.data.repository.TravelDestinationRepository
import com.example.travelwake.data.weather.ArrivalWeatherForecast
import com.example.travelwake.data.weather.WeatherApiService
import com.example.travelwake.engine.AlarmEngine
import com.example.travelwake.engine.AudioTranscriptionHelper
import com.example.travelwake.engine.CustomAlarmSoundEngine
import com.example.travelwake.engine.HapticFeedbackProfile
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.engine.SoundAlertTheme
import com.example.travelwake.engine.WeatherEngine
import com.example.travelwake.ui.map.OfflineMapTileEngine
import com.example.travelwake.ui.map.OfflineTileCacheStats
import com.example.travelwake.ui.map.CachedMapRegion
import com.example.travelwake.ui.map.TileDownloadProgress
import com.example.travelwake.data.model.AppThemeMode
import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.PackItem
import com.example.travelwake.data.model.PackingCategory
import com.example.travelwake.data.model.Reminder
import com.example.travelwake.data.model.SevereWeatherAlert
import com.example.travelwake.data.model.SmartPackingPresets
import com.example.travelwake.data.model.SmartPackingTemplate
import com.example.travelwake.data.model.TodoItem
import com.example.travelwake.data.repository.PackItemRepository
import com.example.travelwake.data.repository.ReminderRepository
import com.example.travelwake.data.repository.TodoRepository
import com.example.travelwake.voice.VoiceCommand
import com.example.travelwake.voice.VoiceCommandExecutor
import com.example.travelwake.voice.VoiceCommandManager
import com.example.travelwake.voice.VoiceLanguage
import com.example.travelwake.service.JourneyMonitoringService
import com.example.travelwake.service.ServiceLocationBridge
import com.example.travelwake.theme.WeatherThemeEngine
import com.example.travelwake.theme.WeatherThemePalette
import com.example.travelwake.theme.WeatherThemeType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TravelWakeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val destinationRepo = TravelDestinationRepository(db.travelDestinationDao())
    private val belongingsRepo = BelongingsRepository(db.belongingDao())
    private val weatherApiService = WeatherApiService()
    private val firebaseRepo = FirebaseRepository(application)
    private val geminiRepo = GeminiRepository()
    private val alarmEngine = AlarmEngine(application)
    private val locationEngine = LocationEngine(application)
    private val audioHelper = AudioTranscriptionHelper(application)
    private val geminiPackingService = GeminiPackingService()

    // Weather-driven Theme Engine state
    private val _weatherTheme = MutableStateFlow<WeatherThemePalette>(WeatherThemeEngine.DefaultPalette)
    val weatherTheme: StateFlow<WeatherThemePalette> = _weatherTheme.asStateFlow()

    private val _autoWeatherThemeEnabled = MutableStateFlow(true)
    val autoWeatherThemeEnabled: StateFlow<Boolean> = _autoWeatherThemeEnabled.asStateFlow()

    // Gemini Smart Packing Checklist state
    private val _tripDurationText = MutableStateFlow("1 Day Trip")
    val tripDurationText: StateFlow<String> = _tripDurationText.asStateFlow()

    private val _geminiPackingSuggestions = MutableStateFlow<List<GeminiPackingSuggestion>>(emptyList())
    val geminiPackingSuggestions: StateFlow<List<GeminiPackingSuggestion>> = _geminiPackingSuggestions.asStateFlow()

    private val _isGeneratingPacking = MutableStateFlow(false)
    val isGeneratingPacking: StateFlow<Boolean> = _isGeneratingPacking.asStateFlow()

    private val _geminiPackingNotice = MutableStateFlow<String?>(null)
    val geminiPackingNotice: StateFlow<String?> = _geminiPackingNotice.asStateFlow()

    // Real-time coordinates for MapView route visualization
    private val _currentUserLatitude = MutableStateFlow(18.5204)
    val currentUserLatitude: StateFlow<Double> = _currentUserLatitude.asStateFlow()

    private val _currentUserLongitude = MutableStateFlow(73.8567)
    val currentUserLongitude: StateFlow<Double> = _currentUserLongitude.asStateFlow()

    // Origin coordinates for progress bar and timeline calculation
    private val _originLatitude = MutableStateFlow(18.5204)
    val originLatitude: StateFlow<Double> = _originLatitude.asStateFlow()

    private val _originLongitude = MutableStateFlow(73.8567)
    val originLongitude: StateFlow<Double> = _originLongitude.asStateFlow()

    // Custom sound alert and haptic feedback selection
    private val _selectedSoundAlertTheme = MutableStateFlow(SoundAlertTheme.HIGH_URGENCY_RADAR)
    val selectedSoundAlertTheme: StateFlow<SoundAlertTheme> = _selectedSoundAlertTheme.asStateFlow()

    private val _selectedHapticProfile = MutableStateFlow(HapticFeedbackProfile.ESCALATING_PULSE)
    val selectedHapticProfile: StateFlow<HapticFeedbackProfile> = _selectedHapticProfile.asStateFlow()

    // Offline Map Tile Engine & Cache Stats
    val customAlarmSoundEngine = CustomAlarmSoundEngine(application)
    val offlineMapTileEngine = OfflineMapTileEngine(application)
    val offlineTileCacheStats: StateFlow<OfflineTileCacheStats> = offlineMapTileEngine.cacheStats
    val tileDownloadProgress: StateFlow<TileDownloadProgress> = offlineMapTileEngine.downloadProgress
    val cachedMapRegions: StateFlow<List<CachedMapRegion>> = offlineMapTileEngine.cachedRegions

    // Voice Command Manager for Hands-Free Alarm & Journey Control
    val voiceCommandManager = VoiceCommandManager(application)

    // Task, Packing & Reminder Persistence Repositories
    val todoRepo = TodoRepository(db.todoDao())
    val packRepo = PackItemRepository(db.packItemDao())
    val reminderRepo = ReminderRepository(db.reminderDao(), application)

    val allTodos: StateFlow<List<TodoItem>> = todoRepo.allTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incompleteTodos: StateFlow<List<TodoItem>> = todoRepo.incompleteTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackItems: StateFlow<List<PackItem>> = packRepo.allPackItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unpackedItems: StateFlow<List<PackItem>> = packRepo.unpackedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<Reminder>> = reminderRepo.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceCommandExecutor = VoiceCommandExecutor(
        todoRepository = todoRepo,
        packItemRepository = packRepo,
        reminderRepository = reminderRepo,
        onSnoozeAlarm = { mins -> snoozeAlarm(mins) },
        onCancelAlarm = { acknowledgeAlarm() },
        onSetAlarmDistance = { dist -> setAlertDistance(dist) },
        onStartJourney = { startJourney() },
        getJourneyInfo = {
            val dest = _selectedDestination.value?.name ?: ""
            val dist = "${_remainingDistanceMeters.value}m away"
            Pair(dest, dist)
        },
        getWeatherWarning = {
            val weather = _destinationWeather.value
            val alert = _activeSevereWeatherAlert.value
            when {
                alert != null -> "${alert.headline}. Take safety precautions."
                weather != null && weather.condition.lowercase().contains("rain") -> "Rain expected at destination. Carry an umbrella and waterproof bag."
                else -> null
            }
        }
    )

    // Severe Weather Alert State (Real-Time Safety & Destination Warnings)
    private val _activeSevereWeatherAlert = MutableStateFlow<SevereWeatherAlert?>(null)
    val activeSevereWeatherAlert: StateFlow<SevereWeatherAlert?> = _activeSevereWeatherAlert.asStateFlow()

    // Dynamic Theme Mode (System auto-switching for night travel / light / dark)
    private val _themeMode = MutableStateFlow<AppThemeMode>(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Auth state
    val currentUser: StateFlow<UserProfile?> = firebaseRepo.currentUser

    // Room flows
    val allJourneys: StateFlow<List<JourneyEntity>> = db.journeyDao().getAllJourneys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPlaces: StateFlow<List<SavedPlaceEntity>> = db.savedPlaceDao().getAllPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val travelDestinations: StateFlow<List<TravelDestinationEntity>> = destinationRepo.allDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val belongings: StateFlow<List<BelongingEntity>> = belongingsRepo.allBelongings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Journey State
    private val _journeyState = MutableStateFlow(JourneyStatus.IDLE)
    val journeyState: StateFlow<JourneyStatus> = _journeyState.asStateFlow()

    private val _selectedDestination = MutableStateFlow<DestinationItem?>(null)
    val selectedDestination: StateFlow<DestinationItem?> = _selectedDestination.asStateFlow()

    // Destination management & settings state
    private val _departurePoint = MutableStateFlow("Current Location (Central Hub)")
    val departurePoint: StateFlow<String> = _departurePoint.asStateFlow()

    private val _scheduledArrivalTime = MutableStateFlow(System.currentTimeMillis() + 1800_000)
    val scheduledArrivalTime: StateFlow<Long> = _scheduledArrivalTime.asStateFlow()

    private val _arrivalTimeFormatted = MutableStateFlow("")
    val arrivalTimeFormatted: StateFlow<String> = _arrivalTimeFormatted.asStateFlow()

    // Alarm Settings
    private val _alertDistanceMeters = MutableStateFlow(500)
    val alertDistanceMeters: StateFlow<Int> = _alertDistanceMeters.asStateFlow()

    private val _alarmSoundEnabled = MutableStateFlow(true)
    val alarmSoundEnabled: StateFlow<Boolean> = _alarmSoundEnabled.asStateFlow()

    private val _alarmVibrationEnabled = MutableStateFlow(true)
    val alarmVibrationEnabled: StateFlow<Boolean> = _alarmVibrationEnabled.asStateFlow()

    private val _progressiveAlarmEnabled = MutableStateFlow(true)
    val progressiveAlarmEnabled: StateFlow<Boolean> = _progressiveAlarmEnabled.asStateFlow()

    private val _transportMode = MutableStateFlow(TransportMode.TRAIN)
    val transportMode: StateFlow<TransportMode> = _transportMode.asStateFlow()

    private val _remainingDistanceMeters = MutableStateFlow(0)
    val remainingDistanceMeters: StateFlow<Int> = _remainingDistanceMeters.asStateFlow()

    private val _etaMinutes = MutableStateFlow(0)
    val etaMinutes: StateFlow<Int> = _etaMinutes.asStateFlow()

    private val _destinationWeather = MutableStateFlow(WeatherInfo())
    val destinationWeather: StateFlow<WeatherInfo> = _destinationWeather.asStateFlow()

    private val _arrivalWeatherForecast = MutableStateFlow<ArrivalWeatherForecast?>(null)
    val arrivalWeatherForecast: StateFlow<ArrivalWeatherForecast?> = _arrivalWeatherForecast.asStateFlow()

    // Gemini AI states
    private val _aiTransitInsight = MutableStateFlow<String?>(null)
    val aiTransitInsight: StateFlow<String?> = _aiTransitInsight.asStateFlow()

    private val _aiLiveConditions = MutableStateFlow<String?>(null)
    val aiLiveConditions: StateFlow<String?> = _aiLiveConditions.asStateFlow()

    private val _aiQuickTip = MutableStateFlow<String?>(null)
    val aiQuickTip: StateFlow<String?> = _aiQuickTip.asStateFlow()

    // Gemini Destination-Specific Travel Tips (transportation advice, customs, emergency contacts)
    private val _destinationTravelTips = MutableStateFlow<String>("")
    val destinationTravelTips: StateFlow<String> = _destinationTravelTips.asStateFlow()

    private val _isLoadingTravelTips = MutableStateFlow(false)
    val isLoadingTravelTips: StateFlow<Boolean> = _isLoadingTravelTips.asStateFlow()

    // Battery-saving adaptive mode
    private val _batterySaverEnabled = MutableStateFlow(true)
    val batterySaverEnabled: StateFlow<Boolean> = _batterySaverEnabled.asStateFlow()
    val batterySaverInfo = ServiceLocationBridge.batterySaverInfo

    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _preAlarmMessage = MutableStateFlow<String?>(null)
    val preAlarmMessage: StateFlow<String?> = _preAlarmMessage.asStateFlow()

    private var journeySimulationJob: Job? = null
    private var activeJourneyId: String = ""
    private var journeyStartTime: Long = 0L

    init {
        // Wire Voice Command Executor
        voiceCommandManager.setExecutor(voiceCommandExecutor)

        // Initialize formatted arrival time
        val initialTime = Date(_scheduledArrivalTime.value)
        _arrivalTimeFormatted.value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(initialTime)

        // Pre-fill initial quick tip with gemini-3.1-flash-lite
        viewModelScope.launch {
            _aiQuickTip.value = geminiRepo.getQuickTravelTip("smart destination alarm")
        }

        // Seed initial history and tasks if empty
        seedInitialHistoryIfNeeded()
        seedDefaultTasksAndPackingIfNeeded()

        // Bridge foreground service location updates to ViewModel
        viewModelScope.launch {
            ServiceLocationBridge.liveDistanceMeters.collect { dist ->
                if (dist != null && _journeyState.value == JourneyStatus.ACTIVE) {
                    updateRemainingDistance(dist)
                }
            }
        }

        // Bridge foreground service alarm trigger to ViewModel
        viewModelScope.launch {
            ServiceLocationBridge.isAlarmTriggered.collect { triggered ->
                if (triggered && _journeyState.value == JourneyStatus.ACTIVE) {
                    _journeyState.value = JourneyStatus.ALARMING
                    alarmEngine.triggerMainAlarm()
                }
            }
        }
    }

    private fun seedInitialHistoryIfNeeded() {
        viewModelScope.launch {
            try {
                val existing = db.journeyDao().getAllJourneys().first()
                if (existing.isEmpty()) {
                    val sampleTrips = listOf(
                        JourneyEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Central Junction Station",
                            address = "Concourse North, Platform 4",
                            latitude = 18.5289,
                            longitude = 73.8744,
                            alertDistanceMeters = 500,
                            transportMode = TransportMode.TRAIN.name,
                            status = JourneyStatus.COMPLETED.name,
                            startedAt = System.currentTimeMillis() - (86400000L + 2520000L),
                            completedAt = System.currentTimeMillis() - 86400000L,
                            distanceRemainingMeters = 0,
                            etaMinutes = 0,
                            temperatureCelsius = 24,
                            weatherCondition = "Partly Cloudy",
                            weatherRecommendation = "Pleasant travel conditions with mild breeze.",
                            departurePoint = "Metro Terminal 1",
                            durationMinutes = 42,
                            travelTips = "🚊 Transit: Direct interchange at concourse.\n🤝 Customs: Stand on left on escalators.\n🚨 Emergency: Transit Security: 139"
                        ),
                        JourneyEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Tech Park Express Terminal",
                            address = "East Gate Interchange",
                            latitude = 18.5590,
                            longitude = 73.7868,
                            alertDistanceMeters = 750,
                            transportMode = TransportMode.METRO.name,
                            status = JourneyStatus.COMPLETED.name,
                            startedAt = System.currentTimeMillis() - (172800000L + 1680000L),
                            completedAt = System.currentTimeMillis() - 172800000L,
                            distanceRemainingMeters = 0,
                            etaMinutes = 0,
                            temperatureCelsius = 29,
                            weatherCondition = "Sunny",
                            weatherRecommendation = "High UV levels expected. Stay hydrated.",
                            departurePoint = "Central Gateway",
                            durationMinutes = 28,
                            travelTips = "🚊 Transit: Feeder shuttle buses every 5 minutes from Gate 2.\n🤝 Customs: Queue politely at exit turnstiles.\n🚨 Emergency: Police: 112"
                        ),
                        JourneyEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Airport City Center Hub",
                            address = "Terminal 2 Arrivals Blvd",
                            latitude = 18.5822,
                            longitude = 73.9197,
                            alertDistanceMeters = 1000,
                            transportMode = TransportMode.BUS.name,
                            status = JourneyStatus.COMPLETED.name,
                            startedAt = System.currentTimeMillis() - (259200000L + 3300000L),
                            completedAt = System.currentTimeMillis() - 259200000L,
                            distanceRemainingMeters = 0,
                            etaMinutes = 0,
                            temperatureCelsius = 20,
                            weatherCondition = "Light Rain",
                            weatherRecommendation = "Rainfall expected at destination. Carry an umbrella.",
                            departurePoint = "Downtown Express Stand",
                            durationMinutes = 55,
                            travelTips = "🚊 Transit: Luggage trolley bays located right outside exit.\n🤝 Customs: Keep bags within designated carriage racks.\n🚨 Emergency: Airport Helpline: 020-26683232"
                        )
                    )
                    sampleTrips.forEach { db.journeyDao().insertJourney(it) }
                }
            } catch (e: Exception) {
                // Ignore initial seeding error
            }
        }
    }

    fun setDeparturePoint(point: String) {
        _departurePoint.value = point.trim()
    }

    fun setArrivalTime(epochMs: Long, formatted: String? = null) {
        _scheduledArrivalTime.value = epochMs
        val displayTime = formatted ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(epochMs))
        _arrivalTimeFormatted.value = displayTime

        // Refresh weather forecast if a destination is selected
        val dest = _selectedDestination.value
        if (dest != null) {
            refreshArrivalWeatherForecast(dest.latitude, dest.longitude, epochMs, dest.name)
        }
    }

    fun setAlarmSettings(distanceMeters: Int, sound: Boolean, vibration: Boolean, progressive: Boolean) {
        _alertDistanceMeters.value = distanceMeters
        _alarmSoundEnabled.value = sound
        _alarmVibrationEnabled.value = vibration
        _progressiveAlarmEnabled.value = progressive
    }

    fun selectDestination(item: DestinationItem) {
        _selectedDestination.value = item
        _journeyState.value = JourneyStatus.DESTINATION_SELECTED

        // Calculate initial distance from current location
        val curLoc = locationEngine.currentLocation.value
        val dist = LocationEngine.calculateDistanceMeters(
            curLoc.latitude, curLoc.longitude,
            item.latitude, item.longitude
        )
        _remainingDistanceMeters.value = dist
        val calculatedEta = LocationEngine.calculateEtaMinutes(dist, _transportMode.value)
        _etaMinutes.value = calculatedEta

        // Automatically estimate arrival time if not explicitly set
        val estimatedArrivalMs = System.currentTimeMillis() + (calculatedEta * 60_000L)
        _scheduledArrivalTime.value = estimatedArrivalMs
        _arrivalTimeFormatted.value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(estimatedArrivalMs))

        // Retrieve forecast from Weather API Service based on arrival time
        refreshArrivalWeatherForecast(item.latitude, item.longitude, estimatedArrivalMs, item.name)

        // Automatically fetch Gemini destination travel tips (transit advice, customs, emergency contacts)
        fetchDestinationTravelTips(item.name, _transportMode.value.name)

        // Enrich with Maps Grounding (gemini-3.5-flash) and Search Grounding (gemini-3.5-flash)
        viewModelScope.launch {
            _aiTransitInsight.value = "Fetching station grounding..."
            val insight = geminiRepo.getPlaceDetailsWithMaps(item.name)
            _aiTransitInsight.value = insight

            val liveSearch = geminiRepo.getLiveConditionsWithSearch(item.name)
            _aiLiveConditions.value = liveSearch
        }
    }

    fun fetchDestinationTravelTips(destName: String, mode: String = _transportMode.value.name) {
        viewModelScope.launch {
            _isLoadingTravelTips.value = true
            val tips = geminiRepo.getDestinationTravelTips(destName, mode)
            _destinationTravelTips.value = tips
            _isLoadingTravelTips.value = false

            if (_journeyState.value == JourneyStatus.ACTIVE) {
                JourneyMonitoringService.updateService(
                    context = getApplication(),
                    distanceMeters = _remainingDistanceMeters.value,
                    etaMinutes = _etaMinutes.value,
                    approaching = _remainingDistanceMeters.value <= _alertDistanceMeters.value,
                    destinationTravelTips = tips
                )
            }
        }
    }

    fun toggleBatterySavingMode() {
        val newSetting = !_batterySaverEnabled.value
        _batterySaverEnabled.value = newSetting
        JourneyMonitoringService.setBatterySavingMode(getApplication(), newSetting)
    }

    fun deleteJourney(journeyId: String) {
        viewModelScope.launch {
            db.journeyDao().deleteJourney(journeyId)
        }
    }

    fun clearJourneyHistory() {
        viewModelScope.launch {
            db.journeyDao().clearHistory()
        }
    }

    private fun refreshArrivalWeatherForecast(lat: Double, lon: Double, arrivalMs: Long, destName: String) {
        viewModelScope.launch {
            val forecast = weatherApiService.getForecastForArrivalTime(lat, lon, arrivalMs, destName)
            _arrivalWeatherForecast.value = forecast

            val fallbackInfo = WeatherEngine.generateWeatherForDestination(destName, lat, lon)
            val updatedWeather = fallbackInfo.copy(
                temperatureCelsius = forecast.temperatureCelsius,
                condition = forecast.condition,
                rainProbability = forecast.rainProbability,
                windSpeedKmh = forecast.windSpeedKmh,
                summary = forecast.recommendationSummary
            )
            _destinationWeather.value = updatedWeather

            // Update Weather Theme Engine based on current destination weather
            if (_autoWeatherThemeEnabled.value) {
                _weatherTheme.value = WeatherThemeEngine.determineWeatherTheme(updatedWeather)
            }

            // Real-time Severe Weather Detection for destination
            val detectedAlert = WeatherEngine.detectSevereWeatherAlert(destName, updatedWeather)
            _activeSevereWeatherAlert.value = detectedAlert
            if (detectedAlert != null && _journeyState.value == JourneyStatus.ACTIVE) {
                JourneyMonitoringService.pushSevereWeatherAlert(
                    context = getApplication(),
                    hazardType = detectedAlert.hazardType,
                    headline = detectedAlert.headline,
                    description = detectedAlert.description,
                    emergencyAction = detectedAlert.emergencyAction,
                    icon = detectedAlert.iconEmoji
                )
            }
        }
    }

    // Room Travel Destination Management
    fun saveCurrentTravelDestination(notes: String = "") {
        val dest = _selectedDestination.value ?: return
        viewModelScope.launch {
            val entity = TravelDestinationEntity(
                id = UUID.randomUUID().toString(),
                destinationName = dest.name,
                destinationAddress = dest.address,
                destinationLatitude = dest.latitude,
                destinationLongitude = dest.longitude,
                departurePoint = _departurePoint.value,
                scheduledArrivalTime = _scheduledArrivalTime.value,
                arrivalTimeFormatted = _arrivalTimeFormatted.value,
                alarmDistanceMeters = _alertDistanceMeters.value,
                alarmSoundEnabled = _alarmSoundEnabled.value,
                alarmVibrationEnabled = _alarmVibrationEnabled.value,
                progressiveAlarmEnabled = _progressiveAlarmEnabled.value,
                transportMode = _transportMode.value.name,
                notes = notes
            )
            destinationRepo.insertDestination(entity)
        }
    }

    fun saveTravelDestination(destination: TravelDestinationEntity) {
        viewModelScope.launch {
            destinationRepo.insertDestination(destination)
        }
    }

    fun updateTravelDestination(destination: TravelDestinationEntity) {
        viewModelScope.launch {
            destinationRepo.updateDestination(destination)
        }
    }

    fun deleteTravelDestination(destination: TravelDestinationEntity) {
        viewModelScope.launch {
            destinationRepo.deleteDestination(destination)
        }
    }

    fun deleteTravelDestinationById(id: String) {
        viewModelScope.launch {
            destinationRepo.deleteDestinationById(id)
        }
    }

    fun selectTravelDestination(dest: TravelDestinationEntity) {
        _departurePoint.value = dest.departurePoint
        _scheduledArrivalTime.value = dest.scheduledArrivalTime
        _arrivalTimeFormatted.value = dest.arrivalTimeFormatted.ifBlank {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dest.scheduledArrivalTime))
        }
        _alertDistanceMeters.value = dest.alarmDistanceMeters
        _alarmSoundEnabled.value = dest.alarmSoundEnabled
        _alarmVibrationEnabled.value = dest.alarmVibrationEnabled
        _progressiveAlarmEnabled.value = dest.progressiveAlarmEnabled

        val mode = try {
            TransportMode.valueOf(dest.transportMode)
        } catch (e: Exception) {
            TransportMode.TRAIN
        }
        _transportMode.value = mode

        val item = DestinationItem(
            id = dest.id,
            name = dest.destinationName,
            address = dest.destinationAddress,
            latitude = dest.destinationLatitude,
            longitude = dest.destinationLongitude,
            placeType = "station",
            distanceKm = 5.0f,
            estimatedMinutes = 15
        )
        selectDestination(item)
    }

    fun setAlertDistance(meters: Int) {
        _alertDistanceMeters.value = meters
    }

    fun setTransportMode(mode: TransportMode) {
        _transportMode.value = mode
        val dist = _remainingDistanceMeters.value
        if (dist > 0) {
            _etaMinutes.value = LocationEngine.calculateEtaMinutes(dist, mode)
        }
    }

    fun startJourney() {
        val dest = _selectedDestination.value ?: return
        activeJourneyId = UUID.randomUUID().toString()
        journeyStartTime = System.currentTimeMillis()
        _journeyState.value = JourneyStatus.ACTIVE

        // Record journey start origin coordinates for progress bar calculation
        _originLatitude.value = _currentUserLatitude.value
        _originLongitude.value = _currentUserLongitude.value

        // Pre-cache offline map tiles for route bounding box
        offlineMapTileEngine.preCacheRouteTiles(
            startLat = _originLatitude.value,
            startLon = _originLongitude.value,
            destLat = dest.latitude,
            destLon = dest.longitude,
            alertBufferMeters = _alertDistanceMeters.value
        )

        val currentForecast = _arrivalWeatherForecast.value
        val weatherCond = currentForecast?.condition ?: _destinationWeather.value.condition
        val weatherTemp = currentForecast?.temperatureCelsius ?: _destinationWeather.value.temperatureCelsius
        val weatherRec = currentForecast?.recommendationSummary ?: _destinationWeather.value.summary

        val journey = JourneyEntity(
            id = activeJourneyId,
            title = dest.name,
            address = dest.address,
            latitude = dest.latitude,
            longitude = dest.longitude,
            alertDistanceMeters = _alertDistanceMeters.value,
            transportMode = _transportMode.value.name,
            status = JourneyStatus.ACTIVE.name,
            startedAt = journeyStartTime,
            distanceRemainingMeters = _remainingDistanceMeters.value,
            etaMinutes = _etaMinutes.value,
            temperatureCelsius = weatherTemp,
            weatherCondition = weatherCond,
            weatherRecommendation = weatherRec,
            departurePoint = _departurePoint.value,
            durationMinutes = 0,
            travelTips = _destinationTravelTips.value
        )

        viewModelScope.launch {
            db.journeyDao().insertJourney(journey)
            firebaseRepo.syncJourneyToFirestore(journey)
        }

        JourneyMonitoringService.startService(
            context = getApplication(),
            destination = dest.name,
            distanceMeters = _remainingDistanceMeters.value,
            etaMinutes = _etaMinutes.value,
            destLat = dest.latitude,
            destLon = dest.longitude,
            alertDistanceMeters = _alertDistanceMeters.value,
            departurePoint = _departurePoint.value,
            arrivalTimeFormatted = _arrivalTimeFormatted.value,
            weatherCondition = weatherCond,
            weatherTemp = weatherTemp,
            weatherRecommendation = weatherRec,
            soundThemeId = _selectedSoundAlertTheme.value.id,
            hapticProfileId = _selectedHapticProfile.value.id,
            destinationTravelTips = _destinationTravelTips.value,
            batterySavingModeEnabled = _batterySaverEnabled.value
        )
    }

    fun updateRemainingDistance(meters: Int) {
        _remainingDistanceMeters.value = meters
        _etaMinutes.value = LocationEngine.calculateEtaMinutes(meters, _transportMode.value)
        val alertDist = _alertDistanceMeters.value

        // Update real-time GPS position coordinates towards destination for MapView visualization
        val dest = _selectedDestination.value
        if (dest != null) {
            val originLat = _originLatitude.value
            val originLon = _originLongitude.value
            val totalDistance = maxOf(LocationEngine.calculateDistanceMeters(originLat, originLon, dest.latitude, dest.longitude), 1000)
            val progress = (1.0 - (meters.toDouble() / totalDistance.toDouble())).coerceIn(0.0, 1.0)
            _currentUserLatitude.value = originLat + (dest.latitude - originLat) * progress
            _currentUserLongitude.value = originLon + (dest.longitude - originLon) * progress
        }

        // Check progressive pre-alarm warnings
        when {
            meters in 950..1050 -> {
                _preAlarmMessage.value = "Destination approaching: 1 km remaining"
                alarmEngine.triggerPreAlarmChime()
            }
            meters in 700..800 -> {
                _preAlarmMessage.value = "Getting closer: 750 m remaining"
                alarmEngine.triggerPreAlarmChime()
            }
            meters in (alertDist - 50)..alertDist -> {
                _preAlarmMessage.value = "WAKE UP SOON: $alertDist m remaining"
            }
        }

        // Trigger Main Alarm when within alert threshold!
        if (meters <= alertDist && _journeyState.value == JourneyStatus.ACTIVE) {
            _journeyState.value = JourneyStatus.ALARMING
            alarmEngine.triggerMainAlarm()
            JourneyMonitoringService.updateService(getApplication(), meters, _etaMinutes.value, true)
        } else if (meters <= alertDist * 1.5) {
            JourneyMonitoringService.updateService(getApplication(), meters, _etaMinutes.value, true)
        } else {
            JourneyMonitoringService.updateService(getApplication(), meters, _etaMinutes.value, false)
        }
    }

    fun acknowledgeAlarm() {
        alarmEngine.stopAlarm()
        _journeyState.value = JourneyStatus.ACKNOWLEDGED
    }

    fun snoozeAlarm(minutes: Int = 2) {
        alarmEngine.stopAlarm()
        _journeyState.value = JourneyStatus.ACTIVE
        // Snooze by temporarily waiting
        viewModelScope.launch {
            delay(minutes * 60_000L) // configurable minutes snooze
            if (_journeyState.value == JourneyStatus.ACTIVE && _remainingDistanceMeters.value <= _alertDistanceMeters.value) {
                _journeyState.value = JourneyStatus.ALARMING
                alarmEngine.triggerMainAlarm()
            }
        }
    }

    fun endJourney() {
        alarmEngine.stopAlarm()
        journeySimulationJob?.cancel()
        _journeyState.value = JourneyStatus.COMPLETED

        JourneyMonitoringService.stopService(getApplication())

        val dest = _selectedDestination.value
        if (dest != null && activeJourneyId.isNotBlank()) {
            val now = System.currentTimeMillis()
            val start = if (journeyStartTime > 0L) journeyStartTime else (now - 1200000L)
            val durationMin = ((now - start) / 60000L).toInt().coerceAtLeast(1)

            val completedJourney = JourneyEntity(
                id = activeJourneyId,
                title = dest.name,
                address = dest.address,
                latitude = dest.latitude,
                longitude = dest.longitude,
                alertDistanceMeters = _alertDistanceMeters.value,
                transportMode = _transportMode.value.name,
                status = JourneyStatus.COMPLETED.name,
                startedAt = start,
                completedAt = now,
                distanceRemainingMeters = 0,
                etaMinutes = 0,
                temperatureCelsius = _destinationWeather.value.temperatureCelsius,
                weatherCondition = _destinationWeather.value.condition,
                weatherRecommendation = _destinationWeather.value.summary,
                departurePoint = _departurePoint.value,
                durationMinutes = durationMin,
                travelTips = _destinationTravelTips.value
            )
            viewModelScope.launch {
                db.journeyDao().updateJourney(completedJourney)
                firebaseRepo.syncJourneyToFirestore(completedJourney)
            }
        }
    }

    fun resetToIdle() {
        alarmEngine.stopAlarm()
        _journeyState.value = JourneyStatus.IDLE
        _selectedDestination.value = null
        _remainingDistanceMeters.value = 0
        _etaMinutes.value = 0
        _preAlarmMessage.value = null
    }

    // Dynamic Belongings Checklist
    fun toggleBelonging(item: BelongingEntity) {
        viewModelScope.launch {
            belongingsRepo.toggleBelonging(item)
        }
    }

    fun addCustomBelonging(name: String, category: String = "Essentials") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val item = BelongingEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                isChecked = false,
                isDefault = false,
                category = category
            )
            belongingsRepo.insertBelonging(item)
        }
    }

    fun deleteBelonging(id: String) {
        viewModelScope.launch {
            belongingsRepo.deleteBelonging(id)
        }
    }

    fun resetBelongingsCheck() {
        viewModelScope.launch {
            belongingsRepo.setAllChecked(false)
        }
    }

    fun checkAllBelongings() {
        viewModelScope.launch {
            belongingsRepo.setAllChecked(true)
        }
    }

    // Gemini Personalized Packing Checklist Functions
    fun setTripDurationText(duration: String) {
        _tripDurationText.value = duration
    }

    fun generatePackingSuggestionsWithGemini() {
        val dest = _selectedDestination.value
        val destName = dest?.name ?: "Upcoming Travel Destination"
        val weather = _destinationWeather.value
        val duration = _tripDurationText.value

        _isGeneratingPacking.value = true
        _geminiPackingNotice.value = null

        viewModelScope.launch {
            try {
                val suggestions = geminiPackingService.suggestPackingChecklist(
                    destinationName = destName,
                    weather = weather,
                    tripDurationText = duration
                )
                _geminiPackingSuggestions.value = suggestions
                _geminiPackingNotice.value = "Personalized with destination weather (${weather.temperatureCelsius}°C, ${weather.condition}) & $duration"
            } catch (e: Exception) {
                _geminiPackingNotice.value = "Generated using smart weather heuristics for $destName"
            } finally {
                _isGeneratingPacking.value = false
            }
        }
    }

    fun toggleSuggestionSelection(suggestionId: String) {
        val current = _geminiPackingSuggestions.value
        _geminiPackingSuggestions.value = current.map {
            if (it.id == suggestionId) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun addSuggestedItemToBelongings(suggestion: GeminiPackingSuggestion) {
        viewModelScope.launch {
            val item = BelongingEntity(
                id = UUID.randomUUID().toString(),
                name = suggestion.name,
                isChecked = false,
                isDefault = false,
                category = suggestion.category
            )
            belongingsRepo.insertBelonging(item)
            // Remove from suggestions list once added
            _geminiPackingSuggestions.value = _geminiPackingSuggestions.value.filter { it.id != suggestion.id }
        }
    }

    fun addAllSelectedSuggestionsToBelongings() {
        val selected = _geminiPackingSuggestions.value.filter { it.isSelected }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            selected.forEach { suggestion ->
                val item = BelongingEntity(
                    id = UUID.randomUUID().toString(),
                    name = suggestion.name,
                    isChecked = false,
                    isDefault = false,
                    category = suggestion.category
                )
                belongingsRepo.insertBelonging(item)
            }
            // Keep only unselected suggestions
            _geminiPackingSuggestions.value = _geminiPackingSuggestions.value.filterNot { it.isSelected }
        }
    }

    // Weather Theme Engine Controls
    fun setManualWeatherTheme(palette: WeatherThemePalette) {
        _autoWeatherThemeEnabled.value = false
        _weatherTheme.value = palette
    }

    fun toggleAutoWeatherTheme(enabled: Boolean) {
        _autoWeatherThemeEnabled.value = enabled
        if (enabled) {
            _weatherTheme.value = WeatherThemeEngine.determineWeatherTheme(_destinationWeather.value)
        }
    }

    // Saved places
    fun addSavedPlace(name: String, address: String, lat: Double, lon: Double, icon: String = "pin") {
        viewModelScope.launch {
            val place = SavedPlaceEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                address = address,
                latitude = lat,
                longitude = lon,
                iconType = icon,
                defaultAlertDistanceMeters = _alertDistanceMeters.value
            )
            db.savedPlaceDao().insertPlace(place)
            firebaseRepo.syncSavedPlaceToFirestore(place)
        }
    }

    fun deleteSavedPlace(id: String) {
        viewModelScope.launch {
            db.savedPlaceDao().deletePlaceById(id)
        }
    }

    // Voice Destination Search with Audio Transcription (gemini-3.5-transcribe)
    fun startVoiceRecording() {
        val started = audioHelper.startRecording()
        _isRecordingAudio.value = started
    }

    fun stopVoiceRecordingAndSearch(onTranscribed: (String) -> Unit) {
        _isRecordingAudio.value = false
        _isTranscribing.value = true
        viewModelScope.launch {
            val bytes = audioHelper.stopRecordingAndGetBytes()
            val text = if (bytes != null && bytes.isNotEmpty()) {
                geminiRepo.transcribeAudio(bytes)
            } else {
                "Central Railway Station"
            }
            _isTranscribing.value = false
            onTranscribed(text)
        }
    }

    // Low-Latency Assistance (gemini-3.1-flash-lite)
    fun askQuickTip(query: String) {
        viewModelScope.launch {
            val tip = geminiRepo.getQuickTravelTip(query)
            _aiQuickTip.value = tip
        }
    }

    // Google Sign-in simulation / Firebase
    fun signInWithGoogle(email: String = "ashokmuddam5@gmail.com") {
        viewModelScope.launch {
            firebaseRepo.signInWithGoogleDemo(email)
        }
    }

    fun signOut() {
        firebaseRepo.signOut()
    }

    // Security Lab / Simulation mode (Section 51 of DESIGN.md)
    fun simulateAdvanceCloser(stepMeters: Int = 500) {
        val current = _remainingDistanceMeters.value
        val next = (current - stepMeters).coerceAtLeast(0)
        updateRemainingDistance(next)
    }

    fun simulateTriggerAlarm() {
        updateRemainingDistance(_alertDistanceMeters.value)
    }

    fun simulateAutoTravel(speedMultiplier: Int = 1) {
        journeySimulationJob?.cancel()
        journeySimulationJob = viewModelScope.launch {
            while (_remainingDistanceMeters.value > 0 && _journeyState.value != JourneyStatus.COMPLETED) {
                delay(1000L / speedMultiplier)
                val current = _remainingDistanceMeters.value
                val step = if (current > 1000) 250 else 100
                updateRemainingDistance((current - step).coerceAtLeast(0))
            }
        }
    }

    fun stopSimulation() {
        journeySimulationJob?.cancel()
    }

    // Custom Alarm Sound & Haptic Alert Controls
    fun setSoundAlertTheme(theme: SoundAlertTheme) {
        _selectedSoundAlertTheme.value = theme
    }

    fun setHapticProfile(profile: HapticFeedbackProfile) {
        _selectedHapticProfile.value = profile
    }

    fun previewSoundAlert(theme: SoundAlertTheme) {
        customAlarmSoundEngine.previewSound(theme)
    }

    fun previewHapticProfile(profile: HapticFeedbackProfile) {
        customAlarmSoundEngine.previewHaptics(profile)
    }

    fun stopSoundAndHapticPreview() {
        customAlarmSoundEngine.stopSound()
        customAlarmSoundEngine.stopHaptics()
    }

    // Offline Map Tile Engine Controls
    fun toggleWeakSignalSimulation(): Boolean {
        return offlineMapTileEngine.toggleWeakSignalSimulation()
    }

    fun preCacheOfflineTilesForRoute() {
        val dest = _selectedDestination.value
        val destLat = dest?.latitude ?: 18.5289
        val destLon = dest?.longitude ?: 73.8744
        offlineMapTileEngine.preCacheRouteTiles(
            startLat = _originLatitude.value,
            startLon = _originLongitude.value,
            destLat = destLat,
            destLon = destLon,
            alertBufferMeters = _alertDistanceMeters.value
        )
    }

    fun downloadOfflineMapRegion(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            offlineMapTileEngine.downloadAndCacheRegion(name, lat, lon)
        }
    }

    fun downloadOfflineTiles(name: String, lat: Double, lon: Double) {
        downloadOfflineMapRegion(name, lat, lon)
    }

    fun deleteCachedMapRegion(regionId: String) {
        offlineMapTileEngine.deleteRegion(regionId)
    }

    fun clearAllOfflineTiles() {
        offlineMapTileEngine.clearAllCachedTiles()
    }

    fun clearOfflineTileCache() {
        clearAllOfflineTiles()
    }

    // Dynamic Theme Mode controls
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    // Severe Weather Alert controls
    fun pushSevereWeatherAlertToNotification(alert: SevereWeatherAlert) {
        JourneyMonitoringService.pushSevereWeatherAlert(
            context = getApplication(),
            hazardType = alert.hazardType,
            headline = alert.headline,
            description = alert.description,
            emergencyAction = alert.emergencyAction,
            icon = alert.iconEmoji
        )
    }

    fun triggerSimulatedSevereWeather(
        hazard: String = "Severe Thunderstorm Warning",
        headline: String = "Destructive winds & torrential downpour approaching",
        emergencyAction: String = "Seek shelter immediately within underground terminal upon arrival"
    ) {
        val simulatedAlert = SevereWeatherAlert(
            hazardType = hazard,
            severity = CautionLevel.ALERT,
            headline = headline,
            description = "Meteorological radar reports active storm cells over destination corridor with localized flash flooding.",
            emergencyAction = emergencyAction,
            iconEmoji = "⛈️"
        )
        _activeSevereWeatherAlert.value = simulatedAlert
        pushSevereWeatherAlertToNotification(simulatedAlert)
    }

    fun dismissSevereWeatherAlert() {
        _activeSevereWeatherAlert.value = null
    }

    // Voice Command Controls (Hands-free alarm, tasks, packing, reminders and journey actions)
    fun startVoiceListening(onActionReceived: ((VoiceCommand) -> Unit)? = null) {
        voiceCommandManager.startListening(onActionReceived)
    }

    fun stopVoiceListening() {
        voiceCommandManager.stopListening()
    }

    fun processVoiceCommandText(text: String) {
        voiceCommandManager.processSpokenPhrase(text)
    }

    fun confirmVoicePendingAction() {
        voiceCommandManager.confirmPendingAction()
    }

    fun dismissVoicePendingAction() {
        voiceCommandManager.dismissPendingAction()
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        voiceCommandManager.setVoiceLanguage(language)
    }

    fun toggleVoiceMute() {
        voiceCommandManager.toggleTtsMute()
    }

    // --- Task (To-Do) Operations ---
    fun addTodo(title: String, priority: String = "NORMAL", category: String = "Trip", dueAt: Long? = null) {
        viewModelScope.launch {
            todoRepo.addTodo(title = title, priority = priority, category = category, dueAt = dueAt)
        }
    }

    fun toggleTodo(id: String, currentCompleted: Boolean) {
        viewModelScope.launch {
            todoRepo.toggleCompleted(id, currentCompleted)
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            todoRepo.deleteTodo(id)
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            todoRepo.clearCompleted()
        }
    }

    // --- Packing Operations ---
    fun addPackItem(name: String, category: String = "Essentials", essential: Boolean = false, quantity: Int = 1) {
        viewModelScope.launch {
            packRepo.addPackItem(name = name, category = category, essential = essential, quantity = quantity)
        }
    }

    fun togglePackItem(id: String, currentPacked: Boolean) {
        viewModelScope.launch {
            packRepo.togglePacked(id, currentPacked)
        }
    }

    fun deletePackItem(id: String) {
        viewModelScope.launch {
            packRepo.deletePackItem(id)
        }
    }

    fun setAllPackItemsPacked(packed: Boolean) {
        viewModelScope.launch {
            packRepo.setAllPacked(packed)
        }
    }

    fun clearAllPackItems() {
        viewModelScope.launch {
            packRepo.deleteAllPackItems()
        }
    }

    fun applySmartPackingTemplate(template: SmartPackingTemplate) {
        viewModelScope.launch {
            val items = template.defaultItems.map { preset ->
                PackItem(
                    id = UUID.randomUUID().toString(),
                    name = preset.name,
                    category = preset.category,
                    essential = preset.essential,
                    quantity = preset.quantity,
                    packed = false
                )
            }
            packRepo.insertAll(items)
        }
    }

    fun applyWeatherPackingRecommendations() {
        viewModelScope.launch {
            val weather = _destinationWeather.value
            val isRainy = weather != null && (weather.condition.contains("rain", ignoreCase = true) || weather.condition.contains("storm", ignoreCase = true))
            val itemsToAdd = if (isRainy) {
                listOf(
                    PackItem(UUID.randomUUID().toString(), "Compact umbrella", category = PackingCategory.WEATHER.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Raincoat / poncho", category = PackingCategory.WEATHER.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Waterproof phone pouch", category = PackingCategory.WEATHER.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Extra dry socks", category = PackingCategory.CLOTHING.label, essential = false)
                )
            } else {
                listOf(
                    PackItem(UUID.randomUUID().toString(), "Sunglasses UV400", category = PackingCategory.TRAVEL.label, essential = false),
                    PackItem(UUID.randomUUID().toString(), "Sunscreen SPF 50", category = PackingCategory.HEALTH.label, essential = false),
                    PackItem(UUID.randomUUID().toString(), "Refillable water bottle", category = PackingCategory.TRAVEL.label, essential = true)
                )
            }
            packRepo.insertAll(itemsToAdd)
        }
    }

    // --- Reminder Operations ---
    fun addReminder(title: String, triggerTime: Long) {
        viewModelScope.launch {
            reminderRepo.addReminder(title, triggerTime)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(id)
        }
    }

    fun toggleReminderCompleted(id: String) {
        viewModelScope.launch {
            reminderRepo.markCompleted(id)
        }
    }

    private fun seedDefaultTasksAndPackingIfNeeded() {
        viewModelScope.launch {
            val existingPack = packRepo.allPackItems.first()
            if (existingPack.isEmpty()) {
                val defaultPackItems = listOf(
                    PackItem(UUID.randomUUID().toString(), "Passport / National ID", category = PackingCategory.DOCUMENTS.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Wallet & transit cards", category = PackingCategory.MONEY.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Phone charger", category = PackingCategory.ELECTRONICS.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Power bank 10,000 mAh", category = PackingCategory.ELECTRONICS.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Laptop & USB-C cable", category = PackingCategory.ELECTRONICS.label, essential = false),
                    PackItem(UUID.randomUUID().toString(), "Backpack / Daypack", category = PackingCategory.TRAVEL.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Warm fleece / Jacket", category = PackingCategory.CLOTHING.label, essential = false),
                    PackItem(UUID.randomUUID().toString(), "Toothbrush & travel kit", category = PackingCategory.TOILETRIES.label, essential = false),
                    PackItem(UUID.randomUUID().toString(), "Prescription medicines", category = PackingCategory.HEALTH.label, essential = true),
                    PackItem(UUID.randomUUID().toString(), "Compact umbrella", category = PackingCategory.WEATHER.label, essential = true)
                )
                packRepo.insertAll(defaultPackItems)
            }

            val existingTodos = todoRepo.allTodos.first()
            if (existingTodos.isEmpty()) {
                val defaultTodos = listOf(
                    TodoItem(UUID.randomUUID().toString(), "Download offline map for destination region", priority = "HIGH", category = "Trip"),
                    TodoItem(UUID.randomUUID().toString(), "Fully charge phone & power bank", priority = "HIGH", category = "Trip"),
                    TodoItem(UUID.randomUUID().toString(), "Verify platform / boarding terminal", priority = "NORMAL", category = "Trip"),
                    TodoItem(UUID.randomUUID().toString(), "Set TravelWake arrival alarm buffer to 500m", priority = "NORMAL", category = "Trip")
                )
                todoRepo.insertAll(defaultTodos)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        alarmEngine.stopAlarm()
        customAlarmSoundEngine.stopSound()
        customAlarmSoundEngine.stopHaptics()
        voiceCommandManager.destroy()
        journeySimulationJob?.cancel()
    }
}
