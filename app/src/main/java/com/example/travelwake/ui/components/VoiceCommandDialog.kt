package com.example.travelwake.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.travelwake.voice.VoiceCommand
import com.example.travelwake.voice.VoiceLanguage
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCommandDialog(
    viewModel: TravelWakeViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onActionExecuted: ((VoiceCommand) -> Unit)? = null
) {
    val isListening by viewModel.voiceCommandManager.isListening.collectAsState()
    val recognizedText by viewModel.voiceCommandManager.recognizedText.collectAsState()
    val rmsVolume by viewModel.voiceCommandManager.rmsVolume.collectAsState()
    val feedback by viewModel.voiceCommandManager.lastActionFeedback.collectAsState()
    val lastResult by viewModel.voiceCommandManager.lastExecutionResult.collectAsState()
    val selectedLang by viewModel.voiceCommandManager.selectedLanguage.collectAsState()
    val isTtsMuted by viewModel.voiceCommandManager.isTtsMuted.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startVoiceListening { command ->
            onActionExecuted?.invoke(command)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = {
        viewModel.stopVoiceListening()
        onDismiss()
    }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("voice_command_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎙️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hands-Free Voice Alarm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.stopVoiceListening()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp).testTag("close_voice_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Voice Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pulsing Mic Icon Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(if (isListening) pulseScale.coerceAtLeast(1f + rmsVolume * 0.4f) else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) listOf(
                                    ProfessionalPrimary.copy(alpha = 0.35f),
                                    ProfessionalPrimary.copy(alpha = 0.08f)
                                ) else listOf(
                                    ProfessionalSurfaceVariant,
                                    ProfessionalSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                if (isListening) 2.dp else 1.dp,
                                if (isListening) ProfessionalPrimary else ProfessionalBorder
                            ),
                            CircleShape
                        )
                        .clickable {
                            if (isListening) viewModel.stopVoiceListening()
                            else viewModel.startVoiceListening()
                        }
                        .testTag("voice_mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone",
                        tint = if (isListening) ProfessionalPrimary else ProfessionalTextSecondary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Listening Status Text
                Text(
                    text = if (isListening) "Listening hands-free..." else "Tap mic to speak",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isListening) ProfessionalPrimary else ProfessionalTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Transcript Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = recognizedText.ifBlank { "Say 'Snooze', 'Cancel alarm', or 'Set alarm 500m'..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp).fillMaxWidth()
                    )
                }

                // Command Feedback Banner
                AnimatedVisibility(
                    visible = feedback != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    feedback?.let { fb ->
                        val isSuccess = fb.startsWith("✓")
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSuccess) SafetyGreenContainer else AlarmRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = fb,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) SafetyGreen else AlarmRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Confirmation Dialog for Destructive Actions
                if (lastResult?.requiresConfirmation == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AlarmRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = lastResult?.pendingActionDescription ?: "Confirm action?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AlarmRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { viewModel.confirmVoicePendingAction() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlarmRed),
                                    modifier = Modifier.testTag("voice_confirm_btn")
                                ) {
                                    Text("Yes, Confirm", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.dismissVoicePendingAction() },
                                    modifier = Modifier.testTag("voice_cancel_btn")
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multilingual Voice Language Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spoken Language",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { viewModel.toggleVoiceMute() },
                        modifier = Modifier.size(32.dp).testTag("voice_mute_btn")
                    ) {
                        Text(
                            text = if (isTtsMuted) "🔇" else "🔊",
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 4
                ) {
                    listOf(
                        VoiceLanguage.ENGLISH to "EN",
                        VoiceLanguage.HINDI to "हिन्दी",
                        VoiceLanguage.TELUGU to "తెలుగు",
                        VoiceLanguage.TAMIL to "தமிழ்"
                    ).forEach { (lang, label) ->
                        val isSel = selectedLang == lang
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setVoiceLanguage(lang) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.padding(2.dp).testTag("voice_lang_${lang.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Voice Command Trigger Chips (Accessibility & Fast Testing Fallback)
                Text(
                    text = "Quick Spoken Commands",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 2
                ) {
                    VoiceChip(
                        label = "🎒 What to pack?",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("what do I need to pack?")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_pack_list"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VoiceChip(
                        label = "📋 Travel tasks",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("what are my travel tasks?")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_tasks"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VoiceChip(
                        label = "➕ Add passport",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("add passport to my pack list")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_add_passport"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VoiceChip(
                        label = "💤 Snooze 2 mins",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("snooze alarm for 2 minutes")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_snooze"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VoiceChip(
                        label = "🛑 Cancel Alarm",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("cancel alarm, I am awake")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_cancel"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VoiceChip(
                        label = "🚀 Start Journey",
                        onClick = {
                            val cmd = viewModel.voiceCommandManager.processSpokenPhrase("start journey")
                            onActionExecuted?.invoke(cmd)
                        },
                        tag = "voice_quick_start"
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceChip(
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
