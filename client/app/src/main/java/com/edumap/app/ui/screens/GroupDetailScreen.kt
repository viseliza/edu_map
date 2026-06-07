package com.edumap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edumap.app.EduMapUiState
import com.edumap.app.data.dto.*

@Composable
fun GroupDetailScreen(
    group: GroupServiceDto,
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onSubjectClick: (EducationPlanResponseDto, EducationSubjectDto) -> Unit
) {
    // Загружаем данные при первом показе
    LaunchedEffect(group.id) {
        if (uiState.educationPlans == null) {
            // Можно вызвать viewModel.loadEducationPlans() здесь
        }
    }
    
    val educationPlans = uiState.educationPlans
    val educationFields = uiState.educationFields
    
    when {
        uiState.isLoading && educationPlans == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null && educationPlans == null -> {
            RetryStateCard(
                title = "Не удалось загрузить данные",
                message = uiState.errorMessage.orEmpty(),
                onRetry = onRetry
            )
        }
        educationPlans != null -> {
            // Находим план и направление для этой группы
            val field = educationFields?.firstOrNull { it.id == group.educationFieldId }
            val plan = educationPlans.firstOrNull { 
                it.educationPlan.educationFieldId == group.educationFieldId 
            }
            
            if (plan != null) {
                GroupDetailContent(
                    group = group,
                    field = field,
                    plan = plan,
                    onBackClick = onBackClick,
                    onSubjectClick = onSubjectClick
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Учебный план не найден")
                }
            }
        }
    }
}

@Composable
private fun GroupDetailContent(
    group: GroupServiceDto,
    field: EducationFieldServiceDto?,
    plan: EducationPlanResponseDto,
    onBackClick: () -> Unit,
    onSubjectClick: (EducationPlanResponseDto, EducationSubjectDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок с кнопкой назад
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Вернуться назад"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${group.name} • ${group.course ?: 1} курс",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (field != null) {
                        Text(
                            text = "${field.speciality} — ${field.profile}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Список предметов
        item {
            Text(
                text = "Учебные предметы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (plan.educationPlan.educationSubjects.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Предметы пока не добавлены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Администратор должен добавить предметы через панель управления.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(plan.educationPlan.educationSubjects, key = { it.id }) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onSubjectClick(plan, subject) }
                )
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: EducationSubjectDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatHours(subject.hours),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть предмет",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}