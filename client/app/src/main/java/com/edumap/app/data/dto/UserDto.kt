package com.edumap.app.data.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String?,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("father_name")
    val fatherName: String?,
    val role: String,
    @SerializedName("credentials_id")
    val credentialsId: Int?,
    @SerializedName("group_id")
    val groupId: Int?,
    val group: GroupInfoDto?
)

data class GroupInfoDto(
    val id: Int,
    val name: String,
    val course: Int?,
    @SerializedName("education_field_id")
    val educationFieldId: Int?
)