package com.example.travelwake.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Task / To-Do entity for journey and personal planning.
 */
@Entity(tableName = "todo_items")
@JsonClass(generateAdapter = true)
data class TodoItem(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueAt: Long? = null,
    val completed: Boolean = false,
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val tripId: String? = null,
    val category: String = "Trip", // Personal, Trip, Journey, General
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Packing item entity with essential tagging and trip association.
 */
@Entity(tableName = "pack_items")
@JsonClass(generateAdapter = true)
data class PackItem(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Int = 1,
    val category: String = "Essentials",
    val packed: Boolean = false,
    val essential: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tripId: String? = null,
    val notes: String = ""
)

/**
 * Time and journey-triggered reminder entity.
 */
@Entity(tableName = "reminders")
@JsonClass(generateAdapter = true)
data class Reminder(
    @PrimaryKey val id: String,
    val title: String,
    val triggerTime: Long,
    val repeatRule: String? = null,
    val tripId: String? = null,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Packing categories for smart organization.
 */
enum class PackingCategory(val label: String, val icon: String) {
    DOCUMENTS("Documents", "📄"),
    ELECTRONICS("Electronics", "🔌"),
    CLOTHING("Clothing", "👕"),
    TOILETRIES("Toiletries", "🧴"),
    HEALTH("Health", "💊"),
    MONEY("Money", "💳"),
    TRAVEL("Travel", "🧳"),
    WORK("Work", "💼"),
    WEATHER("Weather Gear", "☂️"),
    KIDS("Kids", "🧸"),
    OTHER("Other", "📦")
}

/**
 * Template for auto-generating pre-planned trip packing checklists.
 */
data class SmartPackingTemplate(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val defaultItems: List<PackItemPreset>
)

data class PackItemPreset(
    val name: String,
    val category: String,
    val essential: Boolean = false,
    val quantity: Int = 1
)

object SmartPackingPresets {
    val BUSINESS_TRIP = SmartPackingTemplate(
        id = "business",
        title = "Business Trip",
        description = "Laptop, chargers, formal attire, presentation docs",
        icon = "💼",
        defaultItems = listOf(
            PackItemPreset("Laptop", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Laptop charger", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Phone charger", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Passport / National ID", PackingCategory.DOCUMENTS.label, essential = true),
            PackItemPreset("Business cards & documents", PackingCategory.DOCUMENTS.label),
            PackItemPreset("Formal shirts & trousers", PackingCategory.CLOTHING.label),
            PackItemPreset("Toiletries kit", PackingCategory.TOILETRIES.label),
            PackItemPreset("Power bank", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Noise-cancelling headphones", PackingCategory.ELECTRONICS.label)
        )
    )

    val WEEKEND_TRIP = SmartPackingTemplate(
        id = "weekend",
        title = "Weekend Getaway",
        description = "Casual clothes, power bank, compact essentials",
        icon = "🎒",
        defaultItems = listOf(
            PackItemPreset("Phone charger", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Power bank", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("2x Casual shirts/t-shirts", PackingCategory.CLOTHING.label),
            PackItemPreset("Extra socks & undergarments", PackingCategory.CLOTHING.label),
            PackItemPreset("Toothbrush & toothpaste", PackingCategory.TOILETRIES.label),
            PackItemPreset("Comfortable walking shoes", PackingCategory.CLOTHING.label),
            PackItemPreset("Wallet & cards", PackingCategory.MONEY.label, essential = true),
            PackItemPreset("Water bottle", PackingCategory.TRAVEL.label)
        )
    )

    val RAINY_JOURNEY = SmartPackingTemplate(
        id = "rainy",
        title = "Rainy Journey",
        description = "Waterproof protection, umbrella, quick-dry wear",
        icon = "🌧️",
        defaultItems = listOf(
            PackItemPreset("Compact umbrella", PackingCategory.WEATHER.label, essential = true),
            PackItemPreset("Raincoat / poncho", PackingCategory.WEATHER.label, essential = true),
            PackItemPreset("Waterproof phone pouch", PackingCategory.WEATHER.label, essential = true),
            PackItemPreset("Extra dry socks", PackingCategory.CLOTHING.label),
            PackItemPreset("Waterproof backpack cover", PackingCategory.WEATHER.label),
            PackItemPreset("Quick-dry towel", PackingCategory.TOILETRIES.label)
        )
    )

    val TRAIN_JOURNEY = SmartPackingTemplate(
        id = "train",
        title = "Train Journey",
        description = "Travel blanket, earplugs, tickets, snacks, power bank",
        icon = "🚆",
        defaultItems = listOf(
            PackItemPreset("Train e-ticket / PNR printout", PackingCategory.DOCUMENTS.label, essential = true),
            PackItemPreset("Government photo ID", PackingCategory.DOCUMENTS.label, essential = true),
            PackItemPreset("Power bank (charged)", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Earplugs & eye mask", PackingCategory.TRAVEL.label),
            PackItemPreset("Hand sanitizer & wet wipes", PackingCategory.HEALTH.label),
            PackItemPreset("Light travel shawl / blanket", PackingCategory.CLOTHING.label),
            PackItemPreset("Snacks & refillable water", PackingCategory.TRAVEL.label),
            PackItemPreset("Headphones", PackingCategory.ELECTRONICS.label)
        )
    )

    val INTERNATIONAL_TRIP = SmartPackingTemplate(
        id = "international",
        title = "International Trip",
        description = "Passport, visa, currency, universal adapter, medications",
        icon = "✈️",
        defaultItems = listOf(
            PackItemPreset("Passport & Visa documents", PackingCategory.DOCUMENTS.label, essential = true),
            PackItemPreset("Travel insurance copy", PackingCategory.DOCUMENTS.label, essential = true),
            PackItemPreset("Foreign currency & Forex cards", PackingCategory.MONEY.label, essential = true),
            PackItemPreset("Universal plug adapter", PackingCategory.ELECTRONICS.label, essential = true),
            PackItemPreset("Prescription medications & copies", PackingCategory.HEALTH.label, essential = true),
            PackItemPreset("Luggage tags & locks", PackingCategory.TRAVEL.label),
            PackItemPreset("Portable luggage scale", PackingCategory.TRAVEL.label)
        )
    )

    val ALL_PRESETS = listOf(
        BUSINESS_TRIP,
        WEEKEND_TRIP,
        RAINY_JOURNEY,
        TRAIN_JOURNEY,
        INTERNATIONAL_TRIP
    )
}
