package com.example.travelwake.state

import com.example.travelwake.data.model.JourneyStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared JourneyState Machine to track the current alarm & journey progress
 * and prevent duplicate trigger events, invalid transitions, and duplicate voice commands.
 */
class JourneyStateMachine(initialState: JourneyStatus = JourneyStatus.IDLE) {

    private val _currentState = MutableStateFlow(initialState)
    val currentState: StateFlow<JourneyStatus> = _currentState.asStateFlow()

    /**
     * Validates whether a transition from [from] to [to] is permitted.
     */
    fun canTransition(from: JourneyStatus, to: JourneyStatus): Boolean {
        if (from == to) return false

        return when (from) {
            JourneyStatus.IDLE -> when (to) {
                JourneyStatus.DESTINATION_SELECTED, JourneyStatus.READY, JourneyStatus.ACTIVE -> true
                else -> false
            }
            JourneyStatus.DESTINATION_SELECTED -> when (to) {
                JourneyStatus.READY, JourneyStatus.ACTIVE, JourneyStatus.IDLE, JourneyStatus.CANCELLED -> true
                else -> false
            }
            JourneyStatus.READY -> when (to) {
                JourneyStatus.ACTIVE, JourneyStatus.DESTINATION_SELECTED, JourneyStatus.IDLE, JourneyStatus.CANCELLED -> true
                else -> false
            }
            JourneyStatus.ACTIVE -> when (to) {
                JourneyStatus.APPROACHING, JourneyStatus.ALARMING, JourneyStatus.PAUSED, JourneyStatus.RECOVERY, JourneyStatus.COMPLETED, JourneyStatus.CANCELLED -> true
                else -> false
            }
            JourneyStatus.APPROACHING -> when (to) {
                JourneyStatus.ALARMING, JourneyStatus.ACTIVE, JourneyStatus.PAUSED, JourneyStatus.RECOVERY, JourneyStatus.COMPLETED, JourneyStatus.CANCELLED -> true
                else -> false
            }
            JourneyStatus.ALARMING -> when (to) {
                JourneyStatus.ACKNOWLEDGED, JourneyStatus.ACTIVE, JourneyStatus.COMPLETED, JourneyStatus.CANCELLED -> true
                else -> false
            }
            JourneyStatus.ACKNOWLEDGED -> when (to) {
                JourneyStatus.ALARMING, JourneyStatus.ACTIVE, JourneyStatus.COMPLETED, JourneyStatus.CANCELLED, JourneyStatus.IDLE -> true
                else -> false
            }
            JourneyStatus.PAUSED -> when (to) {
                JourneyStatus.ACTIVE, JourneyStatus.CANCELLED, JourneyStatus.IDLE -> true
                else -> false
            }
            JourneyStatus.RECOVERY -> when (to) {
                JourneyStatus.ACTIVE, JourneyStatus.ALARMING, JourneyStatus.COMPLETED, JourneyStatus.CANCELLED, JourneyStatus.IDLE -> true
                else -> false
            }
            JourneyStatus.COMPLETED -> when (to) {
                JourneyStatus.IDLE, JourneyStatus.DESTINATION_SELECTED, JourneyStatus.ACTIVE -> true
                else -> false
            }
            JourneyStatus.CANCELLED -> when (to) {
                JourneyStatus.IDLE, JourneyStatus.DESTINATION_SELECTED, JourneyStatus.ACTIVE -> true
                else -> false
            }
        }
    }

    /**
     * Attempts to transition to the [newStatus]. Returns true if transition was accepted, false otherwise.
     */
    @Synchronized
    fun transitionTo(newStatus: JourneyStatus): Boolean {
        val current = _currentState.value
        if (current == newStatus) return false
        if (canTransition(current, newStatus)) {
            _currentState.value = newStatus
            return true
        }
        return false
    }

    /**
     * Force state update without validation (e.g., on reset or initialization).
     */
    fun reset(status: JourneyStatus = JourneyStatus.IDLE) {
        _currentState.value = status
    }

    /**
     * Validates whether a command like "Start Journey" can be executed in the current state.
     */
    fun canStartJourney(): Boolean {
        val current = _currentState.value
        return current == JourneyStatus.IDLE || current == JourneyStatus.DESTINATION_SELECTED ||
                current == JourneyStatus.READY || current == JourneyStatus.COMPLETED ||
                current == JourneyStatus.CANCELLED
    }

    fun canSnoozeAlarm(): Boolean {
        val current = _currentState.value
        return current == JourneyStatus.ALARMING
    }

    fun canCancelAlarm(): Boolean {
        val current = _currentState.value
        return current == JourneyStatus.ALARMING || current == JourneyStatus.APPROACHING
    }
}
