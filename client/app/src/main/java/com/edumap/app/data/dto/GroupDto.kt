package com.edumap.app.data.dto

import com.google.gson.annotations.SerializedName

data class GroupServiceDto(
    val id: Int,
    val name: String,
    val course: Int?,
    @SerializedName("education_field_id")
    val educationFieldId: Int?
)

data class EducationFieldServiceDto(
    val id: Int,
    val speciality: String,
    val profile: String,
    @SerializedName("education_plan_id")
    val educationPlanId: Int?
)