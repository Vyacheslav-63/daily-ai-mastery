package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LessonRecord
import com.example.ui.components.CyberVoidBackground
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PureWhite

@Composable
fun LessonViewerOverlay(
    lesson: LessonRecord,
    isDeepDiveLoading: Boolean,
    streamingDeepDiveText: String?,
    deepDiveError: String?,
    onGenerateDeepDive: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    CyberVoidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "АРХИВ МОДУЛЯ",
                        style = LabelCapsTextStyle,
                        color = NeonCyan
                    )
                    Text(
                        text = "День ${lesson.dayNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = PureWhite
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 620.dp)
                    ) {
                        LessonContentSection(
                            lesson = lesson,
                            isDeepDiveLoading = isDeepDiveLoading,
                            streamingDeepDiveText = streamingDeepDiveText,
                            deepDiveError = deepDiveError,
                            onGenerateDeepDive = onGenerateDeepDive,
                            onCompleteLesson = null
                        )
                    }
                }
            }
        }
    }
}
