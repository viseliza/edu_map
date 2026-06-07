package com.edumap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edumap.app.EduMapUiState
import com.edumap.app.data.dto.GroupServiceDto

@Composable
fun DisciplinesScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onGroupClick: (GroupServiceDto) -> Unit
) {
    val groupData = uiState.groupData
    
    when {
        uiState.isLoading && groupData == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка групп...")
                }
            }
        }
        uiState.errorMessage != null && groupData == null -> {
            RetryStateCard(
                title = "Не удалось загрузить группы",
                message = uiState.errorMessage.orEmpty(),
                onRetry = onRetry
            )
        }
        groupData != null -> {
            DisciplinesContent(
                groups = groupData.groups,
                onGroupClick = onGroupClick
            )
        }
    }
}

@Composable
private fun DisciplinesContent(
    groups: List<GroupServiceDto>,
    onGroupClick: (GroupServiceDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Дисциплины по группам",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Выберите группу, чтобы увидеть учебные предметы и темы.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (groups.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Группы пока не добавлены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Администратор должен добавить группы через панель управления.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(groups.sortedBy { it.name }, key = { it.id }) { group ->
                GroupCardDisciplines(
                    group = group,
                    onClick = { onGroupClick(group) }
                )
            }
        }
    }
}

@Composable
private fun GroupCardDisciplines(
    group: GroupServiceDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (group.course != null && group.course > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${group.course} курс",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}