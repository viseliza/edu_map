package com.edumap.app.data

import android.util.Log
import com.edumap.app.matchesSearch
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.edumap.app.model.Course
import com.edumap.app.model.Discipline
import com.edumap.app.model.Material
import com.edumap.app.model.Semester
import com.edumap.app.model.Specialty
import com.edumap.app.model.Topic
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
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
import java.net.URLEncoder

object SupabaseClient {

    private const val BASE_URL = "https://pjgkpoxjnrnmzgnhzdfo.supabase.co"
    private const val API_KEY = "sb_publishable_ygXCxCiypg6flKJ9r6M3RA_YHm5eCHk"
    private const val ADMIN_API_URL = "https://admin-panel-edumap.vercel.app/api"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val httpClient = OkHttpClient()

    class DataAccessException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)

    val client = createSupabaseClient(
        supabaseUrl = BASE_URL,
        supabaseKey = API_KEY
    ) {
        install(Postgrest)
    }

    suspend fun getSpecialties(): List<Specialty> = safeList("specialties") {
        client.from("specialties").select().decodeList<Specialty>()
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getCourses(): List<Course> = safeList("courses") {
        client.from("courses").select().decodeList<Course>()
            .sortedWith(compareBy<Course> { it.specialtyId.orEmpty() }.thenBy { it.number })
    }

    suspend fun getSemesters(): List<Semester> = safeList("semesters") {
        client.from("semesters").select().decodeList<Semester>()
            .sortedWith(compareBy<Semester> { it.courseId.orEmpty() }.thenBy { it.number })
    }

    suspend fun getDisciplines(): List<Discipline> = withContext(Dispatchers.IO) {
        try {
            client.from("disciplines").select().decodeList<Discipline>()
                .sortedWith(compareBy<Discipline> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
        } catch (e: Exception) {
            Log.e("SUPABASE", "Error fetching disciplines", e)
            throw mapDataException("disciplines", e)
        }
    }

    suspend fun getDisciplineById(id: String): Discipline? = withContext(Dispatchers.IO) {
        try {
            val result = client.from("disciplines")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<Discipline>()
            result
        } catch (e: Exception) {
            Log.e("SUPABASE", "Error fetching discipline by id: $id", e)
            null
        }
    }

    suspend fun getTopicsByDiscipline(disciplineId: String): List<Topic> = safeList("topics") {
        client.from("topics")
            .select {
                filter {
                    eq("discipline_id", disciplineId)
                }
            }
            .decodeList<Topic>()
            .sortedWith(compareBy<Topic> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
    }

    suspend fun getTopicById(id: String): Topic? = withContext(Dispatchers.IO) {
        try {
            client.from("topics")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<Topic>()
        } catch (e: Exception) {
            Log.e("SUPABASE", "Error fetching topic by id: $id", e)
            null
        }
    }

    suspend fun getTopics(): List<Topic> = safeList("topics") {
        client.from("topics").select().decodeList<Topic>()
            .sortedWith(compareBy<Topic> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
    }

    suspend fun getMaterials(): List<Material> = safeList("materials") {
        client.from("materials").select().decodeList<Material>()
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })
    }

    suspend fun getMaterialsForDiscipline(disciplineId: String): List<Material> = safeList("materials") {
        client.from("materials")
            .select {
                filter {
                    eq("discipline_id", disciplineId)
                }
            }
            .decodeList<Material>()
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })
    }

    suspend fun getMaterialsForTopic(topicId: String): List<Material> = safeList("materials") {
        client.from("materials")
            .select {
                filter {
                    eq("topic_id", topicId)
                }
            }
            .decodeList<Material>()
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })
    }

    suspend fun getProgramData(): ProgramData = withContext(Dispatchers.IO) {
        ProgramData(
            specialties = getSpecialties(),
            courses = getCourses(),
            semesters = getSemesters(),
            disciplines = getDisciplines()
        ).sanitized()
    }

    suspend fun search(query: String): SearchData = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext SearchData()
        }

        SearchData(
            disciplines = getDisciplines().filter {
                matchesSearch(it.name, query) ||
                    matchesSearch(it.description, query) ||
                    matchesSearch(it.terminology, query) ||
                    matchesSearch(it.theory, query) ||
                    matchesSearch(it.applicationArea, query)
            },
            topics = getTopics().filter {
                matchesSearch(it.name, query)
            }
        )
    }

    suspend fun signUp(login: String, email: String, password: String): StudentSession = withContext(Dispatchers.IO) {
        val cleanLogin = normalizeLogin(login)
        val cleanEmail = normalizeEmail(email)
        validateCredentials(cleanLogin, password)
        validateEmail(cleanEmail)
        if (!isLoginAvailable(cleanLogin)) {
            throw IllegalArgumentException("Такой логин уже занят")
        }

        val body = JsonObject().apply {
            addProperty("email", cleanEmail)
            addProperty("password", password)
            add("data", JsonObject().apply {
                addProperty("login", cleanLogin)
                addProperty("email", cleanEmail)
            })
        }
        val session = sessionFromAuthJson(authRequest("/auth/v1/signup", body), cleanLogin)
        createProfile(session, cleanEmail)
        session
    }

    suspend fun signIn(login: String, password: String): StudentSession = withContext(Dispatchers.IO) {
        val cleanLogin = normalizeLogin(login)
        validateCredentials(cleanLogin, password)
        val body = JsonObject().apply {
            addProperty("login", cleanLogin)
            addProperty("password", password)
        }
        val session = sessionFromAuthJson(serverRequest("student-login", body), cleanLogin)
        ensureProfile(session)
    }

    suspend fun refreshSession(session: StudentSession): StudentSession = withContext(Dispatchers.IO) {
        val refreshToken = session.refreshToken
            ?: throw IllegalStateException("Для этой сессии нет ключа обновления. Войдите один раз заново.")
        val body = JsonObject().apply {
            addProperty("refresh_token", refreshToken)
        }
        val refreshed = sessionFromAuthJson(
            authRequest("/auth/v1/token?grant_type=refresh_token", body),
            session.login
        )
        ensureProfile(refreshed)
    }

    suspend fun updateLogin(session: StudentSession, newLogin: String): StudentSession = withContext(Dispatchers.IO) {
        val cleanLogin = normalizeLogin(newLogin)
        validateLogin(cleanLogin)
        if (cleanLogin == session.login) {
            return@withContext session
        }
        if (!isLoginAvailable(cleanLogin)) {
            throw IllegalArgumentException("Такой логин уже занят")
        }

        val authBody = JsonObject().apply {
            add("data", JsonObject().apply { addProperty("login", cleanLogin) })
        }
        updateAuthUser(session.accessToken, authBody)

        val profileBody = JsonObject().apply {
            addProperty("login", cleanLogin)
        }
        restRequest(
            path = "/rest/v1/profiles?user_id=eq.${session.userId.urlEncoded()}",
            method = "PATCH",
            token = session.accessToken,
            body = profileBody,
            prefer = "return=minimal"
        )
        session.copy(login = cleanLogin)
    }

    suspend fun updatePassword(session: StudentSession, newPassword: String) = withContext(Dispatchers.IO) {
        validatePassword(newPassword)
        val body = JsonObject().apply {
            addProperty("password", newPassword)
        }
        updateAuthUser(session.accessToken, body)
        Unit
    }

    suspend fun requestPasswordReset(login: String) = withContext(Dispatchers.IO) {
        val cleanLogin = normalizeLogin(login)
        validateLogin(cleanLogin)
        val body = JsonObject().apply {
            addProperty("login", cleanLogin)
        }
        serverRequest("student-password-recovery", body)
        Unit
    }

    suspend fun getFavoriteDisciplineIds(token: String): Set<String> = withContext(Dispatchers.IO) {
        getFavoriteDisciplineNotes(token).keys
    }

    suspend fun getFavoriteDisciplineNotes(token: String): Map<String, String> = withContext(Dispatchers.IO) {
        val json = restRequest(
            path = "/rest/v1/favorite_disciplines?select=discipline_id,note",
            method = "GET",
            token = token
        ) as? JsonArray ?: return@withContext emptyMap()

        json.mapNotNull { item ->
            val row = item.asJsonObject
            val id = row.get("discipline_id")?.asString ?: return@mapNotNull null
            val note = row.get("note")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            id to note
        }.toMap()
    }

    suspend fun getFavoriteTopicIds(token: String): Set<String> = withContext(Dispatchers.IO) {
        getFavoriteTopicNotes(token).keys
    }

    suspend fun getFavoriteTopicNotes(token: String): Map<String, String> = withContext(Dispatchers.IO) {
        val json = restRequest(
            path = "/rest/v1/favorite_topics?select=topic_id,note",
            method = "GET",
            token = token
        ) as? JsonArray ?: return@withContext emptyMap()

        json.mapNotNull { item ->
            val row = item.asJsonObject
            val id = row.get("topic_id")?.asString ?: return@mapNotNull null
            val note = row.get("note")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            id to note
        }.toMap()
    }

    suspend fun addFavoriteDiscipline(session: StudentSession, disciplineId: String) = withContext(Dispatchers.IO) {
        val body = JsonObject().apply {
            addProperty("user_id", session.userId)
            addProperty("discipline_id", disciplineId)
        }
        restRequest(
            path = "/rest/v1/favorite_disciplines?on_conflict=user_id,discipline_id",
            method = "POST",
            token = session.accessToken,
            body = body,
            prefer = "resolution=merge-duplicates,return=minimal"
        )
        Unit
    }

    suspend fun removeFavoriteDiscipline(session: StudentSession, disciplineId: String) = withContext(Dispatchers.IO) {
        restRequest(
            path = "/rest/v1/favorite_disciplines?user_id=eq.${session.userId.urlEncoded()}&discipline_id=eq.${disciplineId.urlEncoded()}",
            method = "DELETE",
            token = session.accessToken
        )
        Unit
    }

    suspend fun updateFavoriteDisciplineNote(
        session: StudentSession,
        disciplineId: String,
        note: String
    ) = withContext(Dispatchers.IO) {
        val body = JsonObject().apply {
            addProperty("note", note.trim())
        }
        restRequest(
            path = "/rest/v1/favorite_disciplines?user_id=eq.${session.userId.urlEncoded()}&discipline_id=eq.${disciplineId.urlEncoded()}",
            method = "PATCH",
            token = session.accessToken,
            body = body,
            prefer = "return=minimal"
        )
        Unit
    }

    suspend fun addFavoriteTopic(session: StudentSession, topicId: String) = withContext(Dispatchers.IO) {
        val body = JsonObject().apply {
            addProperty("user_id", session.userId)
            addProperty("topic_id", topicId)
        }
        restRequest(
            path = "/rest/v1/favorite_topics?on_conflict=user_id,topic_id",
            method = "POST",
            token = session.accessToken,
            body = body,
            prefer = "resolution=merge-duplicates,return=minimal"
        )
        Unit
    }

    suspend fun updateFavoriteTopicNote(
        session: StudentSession,
        topicId: String,
        note: String
    ) = withContext(Dispatchers.IO) {
        val body = JsonObject().apply {
            addProperty("note", note.trim())
        }
        restRequest(
            path = "/rest/v1/favorite_topics?user_id=eq.${session.userId.urlEncoded()}&topic_id=eq.${topicId.urlEncoded()}",
            method = "PATCH",
            token = session.accessToken,
            body = body,
            prefer = "return=minimal"
        )
        Unit
    }

    suspend fun removeFavoriteTopic(session: StudentSession, topicId: String) = withContext(Dispatchers.IO) {
        restRequest(
            path = "/rest/v1/favorite_topics?user_id=eq.${session.userId.urlEncoded()}&topic_id=eq.${topicId.urlEncoded()}",
            method = "DELETE",
            token = session.accessToken
        )
        Unit
    }

    private suspend fun <T> safeList(tableName: String, request: suspend () -> List<T>): List<T> =
        withContext(Dispatchers.IO) {
            try {
                request()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error fetching $tableName", e)
                throw mapDataException(tableName, e)
            }
        }

    private fun mapDataException(tableName: String, error: Exception): Exception {
        val rootCause = generateSequence(error as Throwable?) { it.cause }.lastOrNull() ?: error
        return if (rootCause is UnknownHostException || rootCause is ConnectException || rootCause is SocketTimeoutException) {
            DataAccessException(
                "Нет подключения к интернету. Проверьте сеть и попробуйте загрузить данные снова.",
                error
            )
        } else if (rootCause is IOException) {
            DataAccessException(
                "Не удалось связаться с сервером EduMap. Проверьте интернет и попробуйте еще раз.",
                error
            )
        } else {
            DataAccessException(
                "Не удалось загрузить данные раздела ${tableName}. Попробуйте обновить экран еще раз.",
                error
            )
        }
    }

    private fun authRequest(path: String, body: JsonObject): JsonObject {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .header("apikey", API_KEY)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        return executeJsonObject(request)
    }

    private fun serverRequest(path: String, body: JsonObject): JsonObject {
        val request = Request.Builder()
            .url("$ADMIN_API_URL/$path")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        return executeJsonObject(request)
    }

    private fun updateAuthUser(token: String, body: JsonObject): JsonObject {
        val request = Request.Builder()
            .url("$BASE_URL/auth/v1/user")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .put(body.toString().toRequestBody(jsonMediaType))
            .build()
        return executeJsonObject(request)
    }

    private fun restRequest(
        path: String,
        method: String,
        token: String,
        body: JsonObject? = null,
        prefer: String? = null
    ) = executeJson(
        Request.Builder()
            .url("$BASE_URL$path")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .apply {
                if (prefer != null) header("Prefer", prefer)
                when (method) {
                    "GET" -> get()
                    "POST" -> post((body ?: JsonObject()).toString().toRequestBody(jsonMediaType))
                    "DELETE" -> delete()
                    else -> method(method, body?.toString()?.toRequestBody(jsonMediaType))
                }
            }
            .build()
    )

    private fun publicRestRequest(
        path: String,
        method: String,
        body: JsonObject? = null,
        prefer: String? = null
    ) = executeJson(
        Request.Builder()
            .url("$BASE_URL$path")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Content-Type", "application/json")
            .apply {
                if (prefer != null) header("Prefer", prefer)
                when (method) {
                    "GET" -> get()
                    "POST" -> post((body ?: JsonObject()).toString().toRequestBody(jsonMediaType))
                    "DELETE" -> delete()
                    else -> method(method, body?.toString()?.toRequestBody(jsonMediaType))
                }
            }
            .build()
    )

    private fun rpcRequest(functionName: String, body: JsonObject) = executeJson(
        Request.Builder()
            .url("$BASE_URL/rest/v1/rpc/$functionName")
            .header("apikey", API_KEY)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
    )

    private fun executeJsonObject(request: Request): JsonObject =
        executeJson(request) as? JsonObject ?: JsonObject()

    private fun executeJson(request: Request): com.google.gson.JsonElement? {
        httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(readErrorMessage(text))
            }
            if (text.isBlank()) return null
            return JsonParser.parseString(text)
        }
    }

    private fun sessionFromAuthJson(json: JsonObject, fallbackLogin: String): StudentSession {
        val token = json.get("access_token")?.asString
            ?: throw IllegalStateException("Supabase не вернул сессию. Для регистрации по логину нужно отключить подтверждение email в Supabase Auth.")
        val user = json.getAsJsonObject("user")
            ?: throw IllegalStateException("Supabase не вернул данные пользователя.")
        val userId = user.get("id")?.asString
            ?: throw IllegalStateException("Supabase не вернул id пользователя.")
        return StudentSession(
            userId = userId,
            login = fallbackLogin,
            accessToken = token,
            refreshToken = json.get("refresh_token")?.takeUnless { it.isJsonNull }?.asString
        )
    }

    private fun createProfile(session: StudentSession, email: String? = null) {
        val body = JsonObject().apply {
            addProperty("user_id", session.userId)
            addProperty("login", session.login)
            if (!email.isNullOrBlank()) {
                addProperty("email", email)
            }
        }
        restRequest(
            path = "/rest/v1/profiles",
            method = "POST",
            token = session.accessToken,
            body = body,
            prefer = "return=minimal"
        )
    }

    private fun ensureProfile(session: StudentSession): StudentSession {
        val profileJson = restRequest(
            path = "/rest/v1/profiles?select=user_id,login,display_name,email&user_id=eq.${session.userId.urlEncoded()}&limit=1",
            method = "GET",
            token = session.accessToken
        ) as? JsonArray
        val profile = profileJson?.firstOrNull()?.asJsonObject
        if (profile != null) {
            return session.copy(login = profile.get("login")?.asString ?: session.login)
        }
        createProfile(session)
        return session
    }

    private fun isLoginAvailable(login: String): Boolean {
        val body = JsonObject().apply {
            addProperty("candidate_login", login)
        }
        return rpcRequest("is_login_available", body)?.asBoolean ?: false
    }

    private fun normalizeLogin(login: String): String = login.trim().lowercase()

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun validateCredentials(login: String, password: String) {
        validateLogin(login)
        validatePassword(password)
    }

    private fun validateLogin(login: String) {
        require(login.length in 3..32) { "Логин должен быть от 3 до 32 символов" }
        require(login.matches(Regex("^[a-z0-9._-]+$"))) {
            "Логин может содержать латинские буквы, цифры, точку, дефис и нижнее подчеркивание"
        }
    }

    private fun validatePassword(password: String) {
        require(password.length in 6..8) { "Пароль должен быть от 6 до 8 символов" }
    }

    private fun validateEmail(email: String) {
        require(email.length in 6..254 && email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
            "Введите корректную почту для восстановления пароля"
        }
    }

    private fun readErrorMessage(text: String): String {
        val message = runCatching {
            val json = JsonParser.parseString(text).asJsonObject
            json.get("msg")?.asString
                ?: json.get("message")?.asString
                ?: json.get("error_description")?.asString
                ?: json.get("error")?.asString
        }.getOrNull() ?: text.ifBlank { "Ошибка запроса Supabase" }
        val prepared = message.lowercase()
        return when {
            "email rate limit exceeded" in prepared ->
                "Supabase ограничил отправку писем. Подождите несколько минут и попробуйте снова."
            "email address" in prepared && "invalid" in prepared ->
                "Введите корректную почту."
            else -> message
        }
    }

    private fun String.urlEncoded(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())
}

data class StudentSession(
    val userId: String,
    val login: String,
    val accessToken: String,
    val refreshToken: String? = null
)

data class ProgramData(
    val specialties: List<Specialty> = emptyList(),
    val courses: List<Course> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val disciplines: List<Discipline> = emptyList()
)

private fun ProgramData.sanitized(): ProgramData {
    val validCourses = courses
        .filter { it.number > 0 }
        .sortedWith(compareBy<Course> { it.specialtyId.orEmpty() }.thenBy { it.number })
    val validCourseIds = validCourses.map { it.id }.toSet()

    val validSemesters = semesters
        .filter { it.number > 0 && it.courseId in validCourseIds }
        .sortedWith(compareBy<Semester> { it.courseId.orEmpty() }.thenBy { it.number })
    val validSemesterIds = validSemesters.map { it.id }.toSet()

    val validDisciplines = disciplines.filter { discipline ->
        discipline.semesterId == null || discipline.semesterId in validSemesterIds
    }

    return copy(
        courses = validCourses,
        semesters = validSemesters,
        disciplines = validDisciplines
    )
}

data class SearchData(
    val disciplines: List<Discipline> = emptyList(),
    val topics: List<Topic> = emptyList()
)
