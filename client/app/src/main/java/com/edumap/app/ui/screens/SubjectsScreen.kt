package com.edumap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edumap.app.EduMapUiState
import com.edumap.app.data.dto.EducationPlanResponseDto
import com.edumap.app.data.dto.EducationSubjectDto

@Composable
fun SubjectsScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onSubjectClick: (EducationPlanResponseDto, EducationSubjectDto) -> Unit
) {
    val educationPlans = uiState.educationPlans

    when {
        uiState.isLoading && educationPlans == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка учебных планов...")
                }
            }
        }

        uiState.errorMessage != null && educationPlans == null -> {
            RetryStateCard(
                title = "Не удалось загрузить учебные планы",
                message = uiState.errorMessage.orEmpty(),
                onRetry = onRetry
            )
        }

        educationPlans != null -> {
            SubjectsContent(
                educationPlans = educationPlans,
                onSubjectClick = onSubjectClick
            )
        }
    }
}

@Composable
private fun SubjectsContent(
    educationPlans: List<EducationPlanResponseDto>,
    onSubjectClick: (EducationPlanResponseDto, EducationSubjectDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Учебные предметы",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Выберите предмет, чтобы увидеть подробную информацию о темах и разделах.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (educationPlans.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Учебные планы пока не добавлены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Администратор должен добавить учебные планы через панель управления.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            educationPlans.forEach { plan ->
                item(key = plan.id) {
                    EducationFieldSection(
                        plan = plan,
                        onSubjectClick = onSubjectClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EducationFieldSection(
    plan: EducationPlanResponseDto,
    onSubjectClick: (EducationPlanResponseDto, EducationSubjectDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок специальности
            Text(
                text = plan.speciality,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = plan.profile,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Список предметов
            plan.educationPlan.educationSubjects.forEach { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onSubjectClick(plan, subject) }
                )
                if (subject != plan.educationPlan.educationSubjects.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
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

internal fun formatHours(hours: String): String {
    // Формат: "К1-К2-К3-К4"
    val parts = hours.split("-")
    return buildString {
        if (parts.size >= 1 && parts[0].toIntOrNull() ?: 0 > 0) {
            append("${parts[0]} ч. лекции")
        }
        if (parts.size >= 2 && parts[1].toIntOrNull() ?: 0 > 0) {
            if (isNotEmpty()) append(", ")
            append("${parts[1]} ч. практика")
        }
        if (parts.size >= 3 && parts[2].toIntOrNull() ?: 0 > 0) {
            if (isNotEmpty()) append(", ")
            append("${parts[2]} ч. лабораторные")
        }
        if (parts.size >= 4 && parts[3].toIntOrNull() ?: 0 > 0) {
            if (isNotEmpty()) append(", ")
            append("${parts[3]} ч. самостоятельная работа")
        }
        if (isEmpty()) append("Часы не указаны")
    }
}