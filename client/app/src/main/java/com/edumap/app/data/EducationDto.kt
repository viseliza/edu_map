//package com.edumap.app.data.dto
//
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class CredentialsDto(
//    val id: Int,
//    val username: String,
//    val password: String
//)
//
//@Serializable
//data class UserDto(
//    val id: Int,
//    val email: String?,
//    @SerialName("first_name")
//    val firstName: String,
//    @SerialName("last_name")
//    val lastName: String,
//    @SerialName("father_name")
//    val fatherName: String?,
//    val role: String,
//    @SerialName("credentials_id")
//    val credentialsId: Int?,
//    @SerialName("group_id")
//    val groupId: Int?
//)
//
//@Serializable
//data class GroupDto(
//    val id: Int,
//    val name: String,
//    val course: Int?,
//    @SerialName("education_field_id")
//    val educationFieldId: Int?
//)
//
//@Serializable
//data class EducationFieldDto(
//    val id: Int,
//    val speciality: String,
//    val profile: String,
//    @SerialName("education_plan_id")
//    val educationPlanId: Int?
//)
//
//@Serializable
//data class EducationPlanDto(
//    val id: Int,
//    val year: Int,
//    @SerialName("education_field_id")
//    val educationFieldId: Int?,
//    @SerialName("education_subjects")
//    val educationSubjects: List<EducationSubjectDto>?
//)
//
//@Serializable
//data class EducationSubjectDto(
//    val id: Int,
//    val name: String,
//    val hours: String,
//    val themes: String, // JSON как строка
//    @SerialName("education_plan_id")
//    val educationPlanId: Int
//)
//
//@Serializable
//data class AuthRequestDto(
//    val username: String,
//    val password: String
//)
//
//@Serializable
//data class AuthResponseDto(
//    val accessToken: String,
//    val user: UserDto
//)