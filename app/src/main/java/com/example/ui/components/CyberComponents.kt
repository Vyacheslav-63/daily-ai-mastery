package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CodeMdTextStyle
import com.example.ui.theme.CodeSmTextStyle
import com.example.ui.theme.CodeTerminalObsidian
import com.example.ui.theme.CyberDeepSlate
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderTopSheen
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.LowDimSlate
import com.example.ui.theme.MidSlate
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OnSurfaceLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.VoidSlate

@Composable
fun CyberVoidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidSlate)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CyberViolet.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.08f),
                        radius = size.maxDimension * 0.55f
                    ),
                    radius = size.maxDimension * 0.55f,
                    center = Offset(size.width * 0.5f, size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.45f),
                        radius = size.maxDimension * 0.42f
                    ),
                    radius = size.maxDimension * 0.42f,
                    center = Offset(size.width * 0.85f, size.height * 0.45f)
                )
            },
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowColor != null) {
        Brush.verticalGradient(
            listOf(glowColor.copy(alpha = 0.6f), glowColor.copy(alpha = 0.22f))
        )
    } else {
        Brush.verticalGradient(
            listOf(GlassBorderTopSheen, GlassBorder)
        )
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (glowColor != null) {
                    Brush.verticalGradient(
                        listOf(
                            CyberDeepSlate.copy(alpha = 0.88f),
                            VoidSlate.copy(alpha = 0.92f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            GlassSurface,
                            CyberDeepSlate.copy(alpha = 0.78f)
                        )
                    )
                }
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape),
        content = content
    )
}

@Composable
fun CyberPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = SurfaceContainerHigh
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) {
                    Brush.linearGradient(listOf(NeonCyan, ElectricTeal))
                } else {
                    Brush.linearGradient(listOf(SurfaceContainerHigh, SurfaceContainerHigh))
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun CyberSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = CyberViolet,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) accent.copy(alpha = 0.55f) else GlassBorder
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = PureWhite
        ),
        modifier = modifier
            .height(50.dp)
            .minimumInteractiveComponentSize()
    ) {
        content()
    }
}

@Composable
fun CategoryPillBadge(
    text: String,
    isAccentViolet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val accent = if (isAccentViolet) CyberPurple else NeonCyan
    val border = if (isAccentViolet) CyberViolet.copy(alpha = 0.4f) else ElectricTeal.copy(alpha = 0.35f)
    val bg = if (isAccentViolet) CyberViolet.copy(alpha = 0.14f) else ElectricTeal.copy(alpha = 0.12f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = CodeSmTextStyle,
            color = accent,
            maxLines = 1
        )
    }
}

@Composable
fun CyberSkeletonLoader(
    modifier: Modifier = Modifier,
    lineCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(lineCount) { idx ->
            val widthFraction = when (idx % 4) {
                0 -> 0.88f
                1 -> 1.0f
                2 -> 0.72f
                else -> 0.94f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(if (idx == 0) 20.dp else 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NeonCyan.copy(alpha = alpha * 0.55f),
                                CyberViolet.copy(alpha = alpha)
                            )
                        )
                    )
            )
        }
    }
}

private sealed interface ArticleBlock {
    data class Heading(val level: Int, val text: String) : ArticleBlock
    data class Paragraph(val text: String) : ArticleBlock
    data class Bullet(val text: String) : ArticleBlock
    data class CodeBlock(val language: String, val code: String) : ArticleBlock
}

@Composable
fun MarkdownCodeArticle(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = rememberBlocks(markdown)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ArticleBlock.Heading -> {
                    Text(
                        text = block.text,
                        style = if (block.level <= 2) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = PureWhite,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                is ArticleBlock.Paragraph -> {
                    Text(
                        text = parseInlineBold(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight
                    )
                }
                is ArticleBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Text(
                            text = parseInlineBold(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is ArticleBlock.CodeBlock -> {
                    CodeTerminalCard(
                        language = block.language,
                        code = block.code
                    )
                }
            }
        }
    }
}

@Composable
fun CodeTerminalCard(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeTerminalObsidian)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
                Text(
                    text = language.ifBlank { "CODE" }.uppercase(),
                    style = LabelCapsTextStyle,
                    color = ElectricTeal
                )
            }
            IconButton(
                onClick = { copyToClipboard(context, code) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = stringResource(R.string.cd_copy_code),
                    tint = MidSlate,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = code,
            style = CodeMdTextStyle,
            color = NeonCyan.copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("code", text))
    Toast.makeText(context, "Код скопирован", Toast.LENGTH_SHORT).show()
}

private fun rememberBlocks(markdown: String): List<ArticleBlock> {
    val result = mutableListOf<ArticleBlock>()
    val lines = markdown.lines()
    var i = 0
    val paragraphBuffer = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraphBuffer.isNotEmpty()) {
            result.add(ArticleBlock.Paragraph(paragraphBuffer.joinToString(" ")))
            paragraphBuffer.clear()
        }
    }

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                flushParagraph()
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                result.add(ArticleBlock.CodeBlock(lang, codeLines.joinToString("\n").trimEnd()))
            }
            trimmed.startsWith("### ") -> {
                flushParagraph()
                result.add(ArticleBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("## ") -> {
                flushParagraph()
                result.add(ArticleBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("# ") -> {
                flushParagraph()
                result.add(ArticleBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                flushParagraph()
                result.add(ArticleBlock.Bullet(trimmed.drop(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                flushParagraph()
                result.add(ArticleBlock.Bullet(trimmed.replaceFirst(Regex("^\\d+\\.\\s+"), "")))
            }
            trimmed.isBlank() -> {
                flushParagraph()
            }
            else -> {
                paragraphBuffer.add(trimmed)
            }
        }
        i++
    }
    flushParagraph()
    return result
}

private fun parseInlineBold(input: String): AnnotatedString {
    return buildAnnotatedString {
        var idx = 0
        while (idx < input.length) {
            val boldStart = input.indexOf("**", idx)
            val codeStart = input.indexOf("`", idx)
            val nextMarker = listOfNotNull(
                boldStart.takeIf { it >= 0 },
                codeStart.takeIf { it >= 0 }
            ).minOrNull()

            if (nextMarker == null) {
                append(input.substring(idx))
                break
            }

            if (nextMarker > idx) {
                append(input.substring(idx, nextMarker))
            }

            if (nextMarker == boldStart) {
                val boldEnd = input.indexOf("**", boldStart + 2)
                if (boldEnd > boldStart) {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = PureWhite)) {
                        append(input.substring(boldStart + 2, boldEnd))
                    }
                    idx = boldEnd + 2
                } else {
                    append("**")
                    idx = boldStart + 2
                }
            } else {
                val codeEnd = input.indexOf("`", codeStart + 1)
                if (codeEnd > codeStart) {
                    withStyle(
                        SpanStyle(
                            fontFamily = CodeMdTextStyle.fontFamily,
                            color = NeonCyan,
                            background = Color.White.copy(alpha = 0.06f)
                        )
                    ) {
                        append(" ${input.substring(codeStart + 1, codeEnd)} ")
                    }
                    idx = codeEnd + 1
                } else {
                    append("`")
                    idx = codeStart + 1
                }
            }
        }
    }
}

data class CyberNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun CyberBottomNavBar(
    tabs: List<CyberNavTab>,
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VoidSlate.copy(alpha = 0.90f))
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .minimumInteractiveComponentSize()
                        .clickable { onSelect(tab.route) }
                        .padding(vertical = 6.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) NeonCyan else LowDimSlate,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) NeonCyan else LowDimSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(if (selected) 4.dp else 0.dp)
                            .clip(CircleShape)
                            .background(if (selected) NeonCyan else Color.Transparent)
                    )
                }
            }
        }
    }
}
