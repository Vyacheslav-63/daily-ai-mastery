package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProxySettings
import com.example.data.UserProgress
import com.example.ui.components.CategoryPillBadge
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.CodeSmTextStyle
import com.example.ui.theme.CodeTerminalObsidian
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.MidSlate
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.VoidSlate

@Composable
fun ProfileScreen(
    displayName: String,
    email: String?,
    progress: UserProgress?,
    settings: ProxySettings,
    onSaveProxySettings: (apiKey: String, proxyUrl: String, modelName: String) -> Unit,
    onResetProxySettings: () -> Unit,
    onToggleSelfPaced: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(settings.customApiKey) }
    var proxyUrlInput by remember { mutableStateOf(settings.customProxyBaseUrl) }
    var modelInput by remember { mutableStateOf(settings.customModelName) }
    var showKey by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        apiKeyInput = settings.customApiKey
        proxyUrlInput = settings.customProxyBaseUrl
        modelInput = settings.customModelName
    }

    val totalAnswers = progress?.totalAnswers ?: 0L
    val correctAnswers = progress?.correctAnswers ?: 0L
    val accuracyPct = if (totalAnswers > 0L) ((correctAnswers * 100L) / totalAnswers).toInt() else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 110.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account & Mastery Stats
                GlassCard(modifier = Modifier.fillMaxWidth(), glowColor = NeonCyan) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.14f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayName.ifBlank { "AI Engineer" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite
                                )
                                if (!email.isNullOrBlank()) {
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MidSlate
                                    )
                                }
                            }
                            CategoryPillBadge(text = "LVL ${((progress?.totalXp ?: 0L) / 300L) + 1L}")
                        }

                        HorizontalDivider(color = GlassBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatTile(
                                label = "СЕРИЯ",
                                value = "${progress?.streakDays ?: 0} дн",
                                accent = NeonAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "ПРОЙДЕНО",
                                value = "${progress?.highestCompletedDay ?: 0} ур",
                                accent = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "ТОЧНОСТЬ",
                                value = "$accuracyPct%",
                                accent = NeonEmerald,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Study Pace Settings
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Свободный темп (снять таймер 24ч)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Text(
                                text = "По умолчанию новый урок открывается раз в 24 часа. Включите, чтобы изучать дни подряд без ожидания.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MidSlate
                            )
                        }
                        Switch(
                            checked = settings.selfPacedMode,
                            onCheckedChange = onToggleSelfPaced,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VoidSlate,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = MidSlate,
                                uncheckedTrackColor = CodeTerminalObsidian
                            ),
                            modifier = Modifier.testTag("self_paced_switch")
                        )
                    }
                }

                // BYOK & Anti-Blocking Proxy Settings
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = CyberViolet
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = CyberPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ОБХОД БЛОКИРОВОК & BYOK",
                                style = LabelCapsTextStyle,
                                color = CyberPurple
                            )
                        }

                        Text(
                            text = stringResource(R.string.profile_settings_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricTeal.copy(alpha = 0.10f))
                                .border(1.dp, ElectricTeal.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.profile_security_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite
                                )
                            }
                        }

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text(stringResource(R.string.profile_api_key_label)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Key, contentDescription = null, tint = NeonCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = stringResource(R.string.cd_toggle_visibility),
                                        tint = MidSlate
                                    )
                                }
                            },
                            visualTransformation = if (showKey) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = cyberTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_api_key_input")
                        )

                        OutlinedTextField(
                            value = proxyUrlInput,
                            onValueChange = { proxyUrlInput = it },
                            label = { Text(stringResource(R.string.profile_proxy_url_label)) },
                            placeholder = { Text("https://gemini-proxy.example.workers.dev", color = MidSlate) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = cyberTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_proxy_url_input")
                        )

                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = { modelInput = it },
                            label = { Text(stringResource(R.string.profile_model_label)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = cyberTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_model_input")
                        )

                        CyberPrimaryButton(
                            onClick = {
                                onSaveProxySettings(apiKeyInput, proxyUrlInput, modelInput)
                                Toast.makeText(
                                    context,
                                    "Настройки сохранены локально на устройстве",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_proxy_settings_button")
                        ) {
                            Text(
                                text = stringResource(R.string.profile_save_settings),
                                fontWeight = FontWeight.Bold,
                                color = VoidSlate
                            )
                        }

                        if (settings.isCustomRouteActive) {
                            CyberSecondaryButton(
                                onClick = {
                                    onResetProxySettings()
                                    Toast.makeText(
                                        context,
                                        "Восстановлен канал Firebase AI Logic",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                accent = MidSlate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reset_proxy_settings_button")
                            ) {
                                Text(stringResource(R.string.profile_reset_settings))
                            }
                        }
                    }
                }

                CyberSecondaryButton(
                    onClick = onSignOut,
                    accent = ErrorCoral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = ErrorCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_sign_out),
                        color = ErrorCoral
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = LabelCapsTextStyle,
            color = MidSlate
        )
        Text(
            text = value,
            style = CodeSmTextStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
            color = accent
        )
    }
}

@Composable
private fun cyberTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CodeTerminalObsidian,
    unfocusedContainerColor = CodeTerminalObsidian,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = GlassBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = MidSlate,
    cursorColor = NeonCyan
)
