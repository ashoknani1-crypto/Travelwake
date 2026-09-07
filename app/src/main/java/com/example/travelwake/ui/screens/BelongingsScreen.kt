package com.example.travelwake.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.ai.GeminiPackingSuggestion
import com.example.travelwake.data.model.PackingCategory
import com.example.travelwake.data.model.SmartPackingPresets
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.ui.ads.NonIntrusiveBannerAd
import com.example.travelwake.ui.components.GeminiPackingAssistantCard
import com.example.travelwake.ui.components.VoiceCommandDialog
import com.example.travelwake.utils.BelongingsShareHelper
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlarmRedContainer
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.LocalWeatherThemePalette
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalDivider
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextMuted
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BelongingsScreen(
    viewModel: TravelWakeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showVoiceModal by remember { mutableStateOf(false) }

    val belongings by viewModel.belongings.collectAsState()
    val packItems by viewModel.allPackItems.collectAsState()
    val todos by viewModel.allTodos.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()

    val weather by viewModel.destinationWeather.collectAsState()
    val destination by viewModel.selectedDestination.collectAsState()
    val tripDuration by viewModel.tripDurationText.collectAsState()
    val geminiSuggestions by viewModel.geminiPackingSuggestions.collectAsState()
    val isGeneratingPacking by viewModel.isGeneratingPacking.collectAsState()
    val geminiNotice by viewModel.geminiPackingNotice.collectAsState()

    val context = LocalContext.current
    var showShareMenu by remember { mutableStateOf(false) }

    // Aggregate counts for badges
    val unpackedPackCount = packItems.count { !it.packed }
    val activeTodosCount = todos.count { !it.completed }
    val pendingRemindersCount = reminders.count { !it.completed }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("belongings_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("belongings_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfessionalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Travel Prep & Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalTextPrimary
                    )
                    Text(
                        text = destination?.name ?: "All Trips Hub",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hands-Free Voice Assistant Button
                IconButton(
                    onClick = { showVoiceModal = true },
                    modifier = Modifier.testTag("prep_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = ProfessionalPrimary
                    )
                }

                // Share & Export Dropdown
                Box {
                    IconButton(
                        onClick = { showShareMenu = true },
                        modifier = Modifier.testTag("share_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Checklist",
                            tint = ProfessionalPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share via Messaging App") },
                            leadingIcon = {
                                Icon(Icons.Default.Send, contentDescription = null, tint = ProfessionalPrimary)
                            },
                            onClick = {
                                showShareMenu = false
                                BelongingsShareHelper.shareViaMessaging(context, destination?.name, belongings)
                            },
                            modifier = Modifier.testTag("share_messaging_menu_item")
                        )
                        DropdownMenuItem(
                            text = { Text("Export as Text File (.txt)") },
                            leadingIcon = {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = ProfessionalPrimary)
                            },
                            onClick = {
                                showShareMenu = false
                                BelongingsShareHelper.exportAsTextFile(context, destination?.name, belongings)
                            },
                            modifier = Modifier.testTag("export_txt_menu_item")
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.checkAllBelongings()
                        viewModel.setAllPackItemsPacked(true)
                    },
                    modifier = Modifier.testTag("check_all_belongings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check All",
                        tint = ProfessionalPrimary
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.resetBelongingsCheck()
                        viewModel.setAllPackItemsPacked(false)
                    },
                    modifier = Modifier.testTag("reset_belongings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Checks",
                        tint = ProfessionalTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Three Navigation Tabs: Packing, Tasks, Reminders
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ProfessionalSurface,
            contentColor = ProfessionalPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ProfessionalPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎒 Packing", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium)
                        if (unpackedPackCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = ProfessionalPrimaryContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$unpackedPackCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfessionalPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("prep_tab_packing")
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📋 Tasks", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium)
                        if (activeTodosCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = AlertAmberContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$activeTodosCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("prep_tab_tasks")
            )

            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏰ Reminders", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Medium)
                        if (pendingRemindersCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = AlarmRedContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$pendingRemindersCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlarmRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("prep_tab_reminders")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content for Each Tab
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> PackingListTabContent(viewModel, belongings, packItems, destination?.name, weather, tripDuration, geminiSuggestions, isGeneratingPacking, geminiNotice, context)
                1 -> TasksTabContent(viewModel, todos)
                2 -> RemindersTabContent(viewModel, reminders)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Non-intrusive banner ad placed safely at the base of the checklists
        NonIntrusiveBannerAd(
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showVoiceModal) {
        VoiceCommandDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceModal = false }
        )
    }
}

/**
 * Tab 1: Packing List Management (Belongings + PackItems + Smart Templates + Weather suggestions)
 */
@Composable
private fun PackingListTabContent(
    viewModel: TravelWakeViewModel,
    belongings: List<com.example.travelwake.data.model.BelongingEntity>,
    packItems: List<com.example.travelwake.data.model.PackItem>,
    destinationName: String?,
    weather: WeatherInfo?,
    tripDuration: String,
    geminiSuggestions: List<GeminiPackingSuggestion>,
    isGeneratingPacking: Boolean,
    geminiNotice: String?,
    context: android.content.Context
) {
    var newItemText by remember { mutableStateOf("") }
    var isEssential by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Essentials") }

    val packedCount = packItems.count { it.packed } + belongings.count { it.isChecked }
    val totalCount = packItems.size + belongings.size
    val progress = if (totalCount > 0) packedCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Progress Overview Card with Direct Share & Export Actions
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(18.dp))
                    .testTag("belongings_progress_card"),
                color = ProfessionalSurface,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Packing Readiness",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "$packedCount of $totalCount items packed",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }

                        Surface(
                            color = if (packedCount == totalCount && totalCount > 0) SafetyGreenContainer else ProfessionalPrimaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (packedCount == totalCount && totalCount > 0) SafetyGreen else ProfessionalPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (packedCount == totalCount && totalCount > 0) SafetyGreen else ProfessionalPrimary,
                        trackColor = ProfessionalSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Row for Sharing and Exporting Checklist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { BelongingsShareHelper.shareViaMessaging(context, destinationName, belongings) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_checklist_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ProfessionalPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share List",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { BelongingsShareHelper.exportAsTextFile(context, destinationName, belongings) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_txt_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ProfessionalBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ProfessionalTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export .txt",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Smart Packing Template Presets & Weather Auto-recommendations
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Smart Packing Templates",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfessionalTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TemplateChip("🎒 Weekend", onClick = { viewModel.applySmartPackingTemplate(SmartPackingPresets.WEEKEND_TRIP) })
                    TemplateChip("💼 Business", onClick = { viewModel.applySmartPackingTemplate(SmartPackingPresets.BUSINESS_TRIP) })
                    TemplateChip("🌧️ Rainy", onClick = { viewModel.applySmartPackingTemplate(SmartPackingPresets.RAINY_JOURNEY) })
                    TemplateChip("🚆 Train", onClick = { viewModel.applySmartPackingTemplate(SmartPackingPresets.TRAIN_JOURNEY) })
                    TemplateChip("✈️ International", onClick = { viewModel.applySmartPackingTemplate(SmartPackingPresets.INTERNATIONAL_TRIP) })
                    TemplateChip("⛅ Weather Prep", onClick = { viewModel.applyWeatherPackingRecommendations() })
                }
            }
        }

        // Add Custom Item Field
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ProfessionalSurface)
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = { Text("Add pack item (e.g. Toothbrush, Power bank)...", color = ProfessionalTextSecondary, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f).testTag("new_belonging_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ProfessionalSurfaceVariant,
                            unfocusedContainerColor = ProfessionalSurfaceVariant,
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder,
                            focusedTextColor = ProfessionalTextPrimary,
                            unfocusedTextColor = ProfessionalTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newItemText.isNotBlank()) {
                                viewModel.addPackItem(
                                    name = newItemText.trim(),
                                    category = selectedCategory,
                                    essential = isEssential
                                )
                                viewModel.addCustomBelonging(newItemText.trim(), selectedCategory)
                                newItemText = ""
                            }
                        },
                        modifier = Modifier.testTag("add_belonging_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { isEssential = !isEssential }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isEssential,
                            onCheckedChange = { isEssential = it },
                            colors = CheckboxDefaults.colors(checkedColor = AlarmRed)
                        )
                        Text("Mark as Essential ⭐", style = MaterialTheme.typography.labelSmall, color = ProfessionalTextPrimary)
                    }

                    Text(
                        text = "Voice: 'Add umbrella to pack list'",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalPrimary
                    )
                }
            }
        }

        // Gemini AI Travel Assistant
        item {
            GeminiPackingAssistantCard(
                destinationName = destinationName ?: "Upcoming Destination",
                weather = weather ?: WeatherInfo(),
                tripDuration = tripDuration,
                suggestions = geminiSuggestions,
                isLoading = isGeneratingPacking,
                notice = geminiNotice,
                onDurationChange = { viewModel.setTripDurationText(it) },
                onGenerate = { viewModel.generatePackingSuggestionsWithGemini() },
                onToggleSuggestion = { viewModel.toggleSuggestionSelection(it) },
                onAddSuggestion = { viewModel.addSuggestedItemToBelongings(it) },
                onAddAllSelected = { viewModel.addAllSelectedSuggestionsToBelongings() }
            )
        }

        // Pack Items from Room Database
        if (packItems.isNotEmpty()) {
            item {
                Text(
                    text = "Luggage Checklist (${packItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            items(packItems, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                        .clickable { viewModel.togglePackItem(item.id, item.packed) }
                        .testTag("pack_item_${item.id}"),
                    color = ProfessionalSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = item.packed,
                                onCheckedChange = { viewModel.togglePackItem(item.id, item.packed) },
                                colors = CheckboxDefaults.colors(checkedColor = ProfessionalPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.packed) ProfessionalTextMuted else ProfessionalTextPrimary,
                                    fontWeight = if (item.packed) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (item.packed) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ProfessionalTextSecondary
                                    )
                                    if (item.essential) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = AlarmRedContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Essential",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AlarmRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deletePackItem(item.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ProfessionalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Legacy Belongings Items
        if (belongings.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personal Belongings (${belongings.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            items(belongings, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                        .clickable { viewModel.toggleBelonging(item) }
                        .testTag("belonging_item_${item.id}"),
                    color = ProfessionalSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { viewModel.toggleBelonging(item) },
                                colors = CheckboxDefaults.colors(checkedColor = ProfessionalPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.isChecked) ProfessionalTextMuted else ProfessionalTextPrimary,
                                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ProfessionalTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteBelonging(item.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ProfessionalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Travel Tasks (To-Dos) Management
 */
@Composable
private fun TasksTabContent(
    viewModel: TravelWakeViewModel,
    todos: List<com.example.travelwake.data.model.TodoItem>
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("NORMAL") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, PENDING, HIGH

    val filteredTodos = when (filterMode) {
        "PENDING" -> todos.filter { !it.completed }
        "HIGH" -> todos.filter { it.priority == "HIGH" }
        else -> todos
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Add Task Input Box
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(16.dp)),
                color = ProfessionalSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Add Travel Task",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("e.g. Check train PNR, download tickets...", color = ProfessionalTextSecondary, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f).testTag("new_todo_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ProfessionalSurfaceVariant,
                                unfocusedContainerColor = ProfessionalSurfaceVariant,
                                focusedBorderColor = ProfessionalPrimary,
                                unfocusedBorderColor = ProfessionalBorder,
                                focusedTextColor = ProfessionalTextPrimary,
                                unfocusedTextColor = ProfessionalTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTodo(
                                        title = newTaskTitle.trim(),
                                        priority = selectedPriority,
                                        category = "Trip"
                                    )
                                    newTaskTitle = ""
                                }
                            },
                            modifier = Modifier.testTag("add_todo_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("NORMAL" to "🟡 Normal", "HIGH" to "🔴 High", "LOW" to "🟢 Low").forEach { (pri, label) ->
                                val isSelected = selectedPriority == pri
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPriority = pri },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ProfessionalPrimaryContainer,
                                        selectedLabelColor = ProfessionalPrimary
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Voice enabled 🎙️",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalPrimary
                        )
                    }
                }
            }
        }

        // Filter chips and Clear Completed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = filterMode == "ALL",
                        onClick = { filterMode = "ALL" },
                        label = { Text("All (${todos.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterMode == "PENDING",
                        onClick = { filterMode = "PENDING" },
                        label = { Text("Pending (${todos.count { !it.completed }})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterMode == "HIGH",
                        onClick = { filterMode = "HIGH" },
                        label = { Text("High Priority (${todos.count { it.priority == "HIGH" }})", fontSize = 11.sp) }
                    )
                }

                if (todos.any { it.completed }) {
                    OutlinedButton(
                        onClick = { viewModel.clearCompletedTodos() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear Done", fontSize = 11.sp)
                    }
                }
            }
        }

        // Tasks List
        if (filteredTodos.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ProfessionalSurface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 No tasks found!", fontWeight = FontWeight.Bold, color = ProfessionalTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add pre-travel tasks or use voice: 'Add buy train snacks to tasks'", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                }
            }
        } else {
            items(filteredTodos, key = { it.id }) { task ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                        .clickable { viewModel.toggleTodo(task.id, task.completed) }
                        .testTag("todo_item_${task.id}"),
                    color = ProfessionalSurface,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = { viewModel.toggleTodo(task.id, task.completed) },
                                colors = CheckboxDefaults.colors(checkedColor = ProfessionalPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (task.completed) ProfessionalTextMuted else ProfessionalTextPrimary,
                                    fontWeight = if (task.completed) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeColor, textColor, label) = when (task.priority) {
                                        "HIGH" -> Triple(AlarmRedContainer, AlarmRed, "HIGH")
                                        "LOW" -> Triple(SafetyGreenContainer, SafetyGreen, "LOW")
                                        else -> Triple(AlertAmberContainer, AlertAmber, "NORMAL")
                                    }
                                    Surface(color = badgeColor, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = textColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ProfessionalTextSecondary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteTodo(task.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = ProfessionalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Trip Reminders Management
 */
@Composable
private fun RemindersTabContent(
    viewModel: TravelWakeViewModel,
    reminders: List<com.example.travelwake.data.model.Reminder>
) {
    var reminderTitle by remember { mutableStateOf("") }
    var selectedDelayMinutes by remember { mutableIntStateOf(30) }

    val dateFormat = remember { SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Notification channel and permission reminder banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProfessionalPrimaryContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ProfessionalPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Automated System Reminders",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalPrimary
                        )
                        Text(
                            text = "Triggers alarms and high-priority heads-up notifications for pre-boarding, packing, or medication checks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextPrimary
                        )
                    }
                }
            }
        }

        // Add Reminder Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(16.dp)),
                color = ProfessionalSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Set Trip Reminder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = reminderTitle,
                            onValueChange = { reminderTitle = it },
                            placeholder = { Text("e.g. Check out hotel, charge phone...", color = ProfessionalTextSecondary, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f).testTag("new_reminder_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ProfessionalSurfaceVariant,
                                unfocusedContainerColor = ProfessionalSurfaceVariant,
                                focusedBorderColor = ProfessionalPrimary,
                                unfocusedBorderColor = ProfessionalBorder,
                                focusedTextColor = ProfessionalTextPrimary,
                                unfocusedTextColor = ProfessionalTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (reminderTitle.isNotBlank()) {
                                    val triggerTime = System.currentTimeMillis() + (selectedDelayMinutes * 60_000L)
                                    viewModel.addReminder(reminderTitle.trim(), triggerTime)
                                    reminderTitle = ""
                                }
                            },
                            modifier = Modifier.testTag("add_reminder_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Reminder")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Delay Chips
                    Text(
                        text = "Remind me in:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15 to "+15 min", 30 to "+30 min", 60 to "+1 hr", 120 to "+2 hr").forEach { (mins, label) ->
                            val isSel = selectedDelayMinutes == mins
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedDelayMinutes = mins },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Reminders List
        if (reminders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ProfessionalSurface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⏰ No scheduled reminders", fontWeight = FontWeight.Bold, color = ProfessionalTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Use voice: 'Remind me to pack charger at 6 AM'", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                val isPast = reminder.triggerTime <= System.currentTimeMillis()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                        .clickable { viewModel.toggleReminderCompleted(reminder.id) }
                        .testTag("reminder_item_${reminder.id}"),
                    color = ProfessionalSurface,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = reminder.completed,
                                onCheckedChange = { viewModel.toggleReminderCompleted(reminder.id) },
                                colors = CheckboxDefaults.colors(checkedColor = ProfessionalPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (reminder.completed) ProfessionalTextMuted else ProfessionalTextPrimary,
                                    fontWeight = if (reminder.completed) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (reminder.completed) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (isPast) AlertAmber else ProfessionalPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = dateFormat.format(Date(reminder.triggerTime)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPast) AlertAmber else ProfessionalTextSecondary
                                    )
                                    if (reminder.completed) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = SafetyGreenContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "Done",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = SafetyGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteReminder(reminder.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Reminder",
                                tint = ProfessionalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = ProfessionalSurfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ProfessionalTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
