// data/EduMapApiClient.kt
package com.edumap.app.data

import android.util.Log
import com.edumap.app.data.dto.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object EduMapApiClient {

    private const val BASE_URL = "http://192.168.3.7:18001/api"
    
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()

    class DataAccessException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)

    // ==================== AUTHENTICATION ====================

    suspend fun signIn(username: String, password: String): Result<StudentSession> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JsonObject().apply {
                addProperty("username", username)
                addProperty("password", password)
            }
            
            val response = executeJsonObject(
                Request.Builder()
                    .url("$BASE_URL/credentials/auth")
                    .post(body.toString().toRequestBody(jsonMediaType))
                    .build()
            )
            
            val accessToken = response.get("accessToken")?.asString
                ?: response.get("access_token")?.asString
                ?: throw IllegalStateException("Сервер не вернул токен доступа")
            
            val userJson = response.getAsJsonObject("user")
            val userId = userJson?.get("id")?.asString 
                ?: response.get("userId")?.asString
                ?: response.get("user_id")?.asString
                ?: "1"
            
            StudentSession(
                userId = userId,
                login = username,
                accessToken = accessToken,
                refreshToken = null
            )
        }
    }

    
    // ==================== USERS ====================

    suspend fun getUserByUsername(username: String): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonObject(
                Request.Builder()
                    .url("$BASE_URL/user/$username")
                    .get()
                    .build()
            )
            gson.fromJson(response, UserDto::class.java)
        }
    }

    // ==================== GROUPS ====================

    suspend fun getAllGroups(): Result<List<GroupServiceDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonArray(
                Request.Builder()
                    .url("$BASE_URL/group")
                    .get()
                    .build()
            )
            response.map { gson.fromJson(it, GroupServiceDto::class.java) }
                .sortedBy { it.name.lowercase() }
        }
    }

    suspend fun getGroupByCredentials(credentials: String): Result<GroupServiceDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonObject(
                Request.Builder()
                    .url("$BASE_URL/group/$credentials")
                    .get()
                    .build()
            )
            gson.fromJson(response, GroupServiceDto::class.java)
        }
    }

    // ==================== EDUCATION DATA ====================

    suspend fun getEducationPlans(): Result<List<EducationPlanResponseDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonArray(
                Request.Builder()
                    .url("$BASE_URL/education-plan")
                    .get()
                    .build()
            )
            response.map { gson.fromJson(it, EducationPlanResponseDto::class.java) }
                .sortedBy { it.speciality.lowercase() }
        }
    }

    suspend fun getEducationPlanByField(fieldId: Int): Result<EducationPlanResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val plans = getEducationPlans().getOrThrow()
            plans.firstOrNull { it.educationPlan.educationFieldId == fieldId }
                ?: throw IllegalStateException("Учебный план не найден")
        }
    }

    suspend fun getEducationFields(): Result<List<EducationFieldServiceDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonArray(
                Request.Builder()
                    .url("$BASE_URL/education-field")
                    .get()
                    .build()
            )
            response.map { gson.fromJson(it, EducationFieldServiceDto::class.java) }
        }
    }

    suspend fun getEducationFieldsAll(): Result<List<EducationFieldServiceDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = executeJsonArray(
                Request.Builder()
                    .url("$BASE_URL/education-field/all")
                    .get()
                    .build()
            )
            response.map { gson.fromJson(it, EducationFieldServiceDto::class.java) }
        }
    }

    suspend fun getEducationPlanByGroup(groupId: Int): Result<EducationPlanResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val group = getGroupByCredentials(groupId.toString()).getOrThrow()
            val fieldId = group.educationFieldId
                ?: throw IllegalStateException("У группы нет направления")

            val plans = getEducationPlans().getOrThrow()
            plans.firstOrNull { it.educationPlan.educationFieldId == fieldId }
                ?: throw IllegalStateException("Учебный план не найден для направления $fieldId")
        }
    }

    // Получить предметы по группе (упрощённый метод)
    suspend fun getSubjectsByGroup(groupId: Int): Result<List<EducationSubjectDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val plan = getEducationPlanByGroup(groupId).getOrThrow()
            plan.educationPlan.educationSubjects ?: emptyList()
        }
    }

    // ==================== HELPERS ====================

    private fun executeJsonObject(request: Request): JsonObject {
        return executeJson(request) as? JsonObject ?: JsonObject()
    }

    private fun executeJsonArray(request: Request): com.google.gson.JsonArray {
        return executeJson(request) as? com.google.gson.JsonArray ?: com.google.gson.JsonArray()
    }

    private fun executeJson(request: Request): com.google.gson.JsonElement? {
        httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e("EDUMAP_API", "Error: ${response.code} - $text")
                throw IllegalStateException("Ошибка API: ${response.code}")
            }
            if (text.isBlank()) return null
            return JsonParser.parseString(text)
        }
    }
}