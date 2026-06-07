// data/dto/EducationPlanDto.kt
package com.edumap.app.data.dto

import com.google.gson.annotations.SerializedName

data class EducationPlanResponseDto(
    val id: Int,
    val speciality: String,
    val profile: String,
    @SerializedName("education_plan")
    val educationPlan: EducationPlanDataDto
)

data class EducationPlanDataDto(
    val id: Int,
    val year: Int,
    @SerializedName("education_field_id")
    val educationFieldId: Int,
    @SerializedName("education_subjects")
    val educationSubjects: List<EducationSubjectDto>
)

data class EducationSubjectDto(
    val id: Int,
    val name: String,
    val hours: String,
    val themes: List<ThemeSectionDto>,
    @SerializedName("education_plan_id")
    val educationPlanId: Int
)

data class ThemeSectionDto(
    val section: String,
    val hours: String,
    val competencies: String,
    val topics: List<ThemeTopicDto>
)

data class ThemeTopicDto(
    @SerializedName("topic_key")
    val topicKey: String,
    val topic: String,
    @SerializedName("hours_total")
    val hoursTotal: Int,
    val items: List<ThemeItemDto>
)

data class ThemeItemDto(
    val kind: String,
    val text: String,
    val hours: String,
    val competencies: String
)