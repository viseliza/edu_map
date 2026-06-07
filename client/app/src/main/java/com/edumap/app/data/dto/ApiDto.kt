package com.edumap.app.data.dto

import com.google.gson.annotations.SerializedName

data class AuthRequestDto(
    val username: String,
    val password: String
)

data class AuthResponseDto(
    @SerializedName("accessToken") // или "access_token", зависит от ответа NestJS
    val accessToken: String,
    val user: UserDto?
)


data class GroupDto(
    val id: Int,
    val name: String,
    val course: Int?,
    @SerializedName("education_field_id")
    val educationFieldId: Int?
)

data class EducationFieldDto(
    val id: Int,
    val speciality: String,
    val profile: String,
    @SerializedName("education_plan_id")
    val educationPlanId: Int?
)

data class EducationPlanDto(
    val id: Int,
    val year: Int,
    @SerializedName("education_field_id")
    val educationFieldId: Int?,
    @SerializedName("education_subjects")
    val educationSubjects: List<EducationSubjectDto>?
)

//data class EducationSubjectDto(
//    val id: Int,
//    val name: String,
//    val hours: String,
//    val themes: String,
//    @SerializedName("education_plan_id")
//    val educationPlanId: Int
//)