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
import com.edumap.app.data.dto.EducationFieldServiceDto

@Composable
fun GroupsScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onGroupClick: (GroupServiceDto) -> Unit
) {
    val groupData = uiState.groupData
    
    when {
        uiState.isLoading && groupData == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
            GroupsContent(
                groups = groupData.groups,
                fields = groupData.fields,
                onGroupClick = onGroupClick
            )
        }
    }
}

@Composable
private fun GroupsContent(
    groups: List<GroupServiceDto>,
    fields: List<EducationFieldServiceDto>,
    onGroupClick: (GroupServiceDto) -> Unit
) {
    val groupsByField = groups.groupBy { it.educationFieldId }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Группы",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Список специальностей и направлений, и групп которые в них состоят",
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
            // Группы без направления
            groupsByField[null]?.sortedBy { it.name }?.forEach { group ->
                item(key = group.id) {
                    GroupCard(group = group, fieldName = null, onClick = { onGroupClick(group) })
                }
            }
            
            // Группы по направлениям
            groupsByField.filterKeys { it != null }.forEach { (fieldId, fieldGroups) ->
                val field = fields.firstOrNull { it.id == fieldId }
                val fieldName = field?.let { "${it.speciality} - ${it.profile}" } ?: "Направление $fieldId"
                
                item(key = "header_$fieldId") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fieldName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(fieldGroups.sortedBy { it.name }, key = { it.id }) { group ->
                    GroupCard(group = group, fieldName = null, onClick = { onGroupClick(group) })
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: GroupServiceDto,
    fieldName: String?,
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
            
            if (fieldName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fieldName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RetryStateCard(
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Повторить")
            }
        }
    }
}