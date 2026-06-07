package com.edumap.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Specialty(
    val id: String,
    val name: String
)

@Serializable
data class Course(
    val id: String,
    val number: Int,
    @SerialName("specialty_id")
    val specialtyId: String? = null
)

@Serializable
data class Semester(
    val id: String,
    val number: Int,
    @SerialName("course_id")
    val courseId: String? = null
)

@Serializable
data class Discipline(
    val id: String,
    val name: String,
    val description: String? = null,
    val terminology: String? = null,
    val theory: String? = null,
    @SerialName("application_area")
    val applicationArea: String? = null,
    @SerialName("order_index")
    val orderIndex: Int? = null,
    @SerialName("semester_id")
    val semesterId: String? = null
)

@Serializable
data class Topic(
    val id: String,
    val name: String,
    @SerialName("order_index")
    val orderIndex: Int? = null,
    @SerialName("discipline_id")
    val disciplineId: String? = null
)

@Serializable
data class Material(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val url: String? = null,
    @SerialName("order_index")
    val orderIndex: Int? = null,
    @SerialName("topic_id")
    val topicId: String? = null,
    @SerialName("discipline_id")
    val disciplineId: String? = null
)

@Serializable
data class Profile(
    @SerialName("user_id")
    val userId: String,
    val login: String,
    val email: String? = null,
    @SerialName("display_name")
    val displayName: String? = null
)

@Serializable
data class FavoriteDiscipline(
    @SerialName("user_id")
    val userId: String,
    @SerialName("discipline_id")
    val disciplineId: String,
    val note: String? = null
)

@Serializable
data class FavoriteTopic(
    @SerialName("user_id")
    val userId: String,
    @SerialName("topic_id")
    val topicId: String,
    val note: String? = null
)
