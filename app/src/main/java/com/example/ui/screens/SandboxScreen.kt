package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ai.ProxyProbeResult
import com.example.ai.SandboxResult
import com.example.data.ProxySettings
import com.example.data.UiState
import com.example.ui.components.CategoryPillBadge
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.CyberSkeletonLoader
import com.example.ui.components.GlassCard
import com.example.ui.components.MarkdownCodeArticle
import com.example.ui.theme.CodeSmTextStyle
import com.example.ui.theme.CodeTerminalObsidian
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.MidSlate
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.VoidSlate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SandboxScreen(
    settings: ProxySettings,
    sandboxState: UiState<SandboxResult>?,
    probeState: UiState<ProxyProbeResult>?,
    onRunPrompt: (String) -> Unit,
    onTestConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by rememberSaveable { mutableStateOf("") }

    val quickPrompts = listOf(
        "Схема MCP-сервера на Python",
        "Cloudflare Worker для Gemini API",
        "Чек-лист защиты от Prompt Injection",
        "Разница WireGuard и SOCKS5"
    )

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
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = if (settings.isCustomRouteActive) CyberViolet else NeonCyan
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "AI TERMINAL & PROXY LAB",
                                    style = LabelCapsTextStyle,
                                    color = NeonCyan
                                )
                                Text(
                                    text = stringResource(R.string.sandbox_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PureWhite
                                )
                            }
                            CategoryPillBadge(
                                text = if (settings.isCustomRouteActive) "CUSTOM PROXY" else "FIREBASE AI",
                                isAccentViolet = settings.isCustomRouteActive
                            )
                        }

                        Text(
                            text = if (settings.isCustomRouteActive) {
                                "Активный маршрут: ${settings.customProxyBaseUrl.ifBlank { "generativelanguage.googleapis.com" }} (${settings.customModelName})"
                            } else {
                                "Активный маршрут: защищённый канал Firebase AI Logic (App Check)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MidSlate
                        )

                        CyberSecondaryButton(
                            onClick = onTestConnection,
                            enabled = probeState !is UiState.Loading,
                            accent = ElectricTeal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sandbox_test_connection_button")
                        ) {
                            if (probeState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Проверка задержки…")
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.NetworkCheck,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.sandbox_test_connection))
                            }
                        }

                        when (probeState) {
                            is UiState.Success -> {
                                val res = probeState.data
                                val color = if (res.success) NeonEmerald else ErrorCoral
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color.copy(alpha = 0.12f))
                                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = res.message,
                                        style = CodeSmTextStyle,
                                        color = color
                                    )
                                }
                            }
                            is UiState.Error -> {
                                Text(
                                    text = probeState.message,
                                    style = CodeSmTextStyle,
                                    color = ErrorCoral
                                )
                            }
                            else -> Unit
                        }
                    }
                }

                // Prompt Input Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "БЫСТРЫЕ ШАБЛОНЫ",
                            style = LabelCapsTextStyle,
                            color = MidSlate
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickPrompts.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, GlassBorder, RoundedCornerShape(9999.dp))
                                        .minimumInteractiveComponentSize()
                                        .clickable {
                                            promptText = preset
                                            onRunPrompt(preset)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        style = CodeSmTextStyle,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.sandbox_prompt_hint),
                                    color = MidSlate
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CodeTerminalObsidian,
                                unfocusedContainerColor = CodeTerminalObsidian,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                cursorColor = NeonCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sandbox_prompt_input")
                        )

                        CyberPrimaryButton(
                            onClick = { onRunPrompt(promptText) },
                            enabled = promptText.isNotBlank() && sandboxState !is UiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sandbox_run_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = VoidSlate
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.sandbox_run),
                                fontWeight = FontWeight.Bold,
                                color = VoidSlate
                            )
                        }
                    }
                }

                // Terminal Output
                when (sandboxState) {
                    is UiState.Loading -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = CyberViolet
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "ВЫПОЛНЕНИЕ ЗАПРОСА…",
                                    style = LabelCapsTextStyle,
                                    color = CyberPurple
                                )
                                CyberSkeletonLoader(lineCount = 5)
                            }
                        }
                    }
                    is UiState.Success -> {
                        val res = sandboxState.data
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sandbox_output_card"),
                            glowColor = NeonCyan
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Terminal,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = res.routeLabel,
                                            style = LabelCapsTextStyle,
                                            color = ElectricTeal
                                        )
                                    }
                                    CategoryPillBadge(text = "${res.latencyMs} MS")
                                }
                                MarkdownCodeArticle(markdown = res.text)
                            }
                        }
                    }
                    is UiState.Error -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = ErrorCoral
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ОШИБКА ЗАПРОСА",
                                    style = LabelCapsTextStyle,
                                    color = ErrorCoral
                                )
                                Text(
                                    text = sandboxState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorCoral
                                )
                            }
                        }
                    }
                    null -> Unit
                }
            }
        }
    }
}
