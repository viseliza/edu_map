package com.edumap.app.data.api

import com.edumap.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface EduMapApiService {
    
    // Авторизация
    @POST("/api/credentials/auth")
    suspend fun authenticate(@Body credentials: AuthRequestDto): Response<AuthResponseDto>
    
    // Группы
    @GET("/api/group")
    suspend fun getAllGroups(): Response<List<GroupDto>>
    
    @GET("/api/group/{credentials}")
    suspend fun getGroupByCredentials(@Path("credentials") credentials: String): Response<GroupDto>
    
    // Учебные планы
    @GET("/api/education-plan")
    suspend fun getEducationPlans(): Response<List<EducationPlanDto>>
    
    // Направления
    @GET("/api/education-field")
    suspend fun getEducationFields(): Response<List<EducationFieldDto>>
}