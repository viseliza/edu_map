package com.edumap.app


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.edumap.app.data.ProgramData
import com.edumap.app.data.SearchData
import com.edumap.app.data.StudentSession
import com.edumap.app.data.SupabaseClient
import com.edumap.app.model.Discipline
import com.edumap.app.model.Material
import com.edumap.app.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.edumap.app.data.EduMapApiClient
import com.edumap.app.data.dto.GroupServiceDto
import com.edumap.app.data.dto.EducationFieldServiceDto
import com.edumap.app.data.dto.EducationPlanResponseDto
import com.edumap.app.data.dto.UserDto

class EduMapViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStorage = (application as EduMapApplication).sessionStorage
    private val _uiState = MutableStateFlow(EduMapUiState())
    val uiState: StateFlow<EduMapUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
        refresh()
    }

    private fun restoreSession() {
        val saved = sessionStorage.load() ?: return
        _uiState.update { it.copy(session = saved) }
        // Избранное загрузится автоматически в refresh() -> refreshFavorites()
    }

    private fun handleError(error: Throwable): String {
        // Сетевые ошибки — проверяем по типу исключения
        if (isNetworkError(error)) {
            return "Нет подключения к интернету. Проверьте сеть и попробуйте снова."
        }

        val message = error.message ?: ""
        val prepared = message.lowercase()

        if (
            "refresh token" in prepared ||
            "invalid_grant" in prepared ||
            "no key update" in prepared ||
            "ключа обновления" in prepared
        ) {
            sessionStorage.clear()
            _uiState.update {
                it.copy(
                    session = null,
                    favoriteDisciplineIds = emptySet(),
                    favoriteTopicIds = emptySet(),
                    favoriteDisciplineNotes = emptyMap(),
                    favoriteTopicNotes = emptyMap(),
                    authMessage = "Войдите один раз заново. После этого приложение будет сохранять вход автоматически."
                )
            }
            return "Войдите один раз заново. После этого приложение будет сохранять вход автоматически."
        }
        if ("jwt expired" in prepared || "invalid jwt" in prepared || "not authenticated" in prepared) {
            return "Сессия устарела. Приложение попробует обновить вход автоматически."
        }
        // Текстовые признаки сетевых ошибок как запасной вариант
        if (
            "нет подключения к интернету" in prepared ||
            "проверьте сеть" in prepared ||
            "failed to connect" in prepared ||
            "unable to resolve host" in prepared ||
            "timeout" in prepared ||
            "connection refused" in prepared ||
            "network" in prepared
        ) {
            return "11 Нет подключения к интернету. Проверьте сеть и попробуйте снова."
        }
        return message.ifBlank { "Произошла ошибка" }
    }

    private fun isNetworkError(error: Throwable): Boolean {
        var cause: Throwable? = error
        repeat(5) {
            when (cause) {
                is java.net.UnknownHostException,
                is java.net.SocketTimeoutException,
                is java.net.ConnectException,
                is java.io.IOException -> return true
                else -> cause = cause?.cause
            }
        }
        return false
    }

    private suspend fun refreshSessionIfPossible(session: StudentSession): StudentSession {
        if (session.refreshToken.isNullOrBlank()) {
            return session
        }
        val refreshed = SupabaseClient.refreshSession(session)
        sessionStorage.save(refreshed)
        _uiState.update { it.copy(session = refreshed) }
        return refreshed
    }

    private suspend fun requireFreshSession(message: String): StudentSession {
        val session = _uiState.value.session ?: throw IllegalStateException(message)
        return refreshSessionIfPossible(session)
    }

    fun clearAuthMessage() {
        _uiState.update { it.copy(authMessage = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val programData = SupabaseClient.getProgramData()
                val topics = SupabaseClient.getTopics()
                val materials = SupabaseClient.getMaterials()
                EduMapContent(
                    programData = programData,
                    topics = topics,
                    materials = materials
                )
            }.onSuccess { content ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        content = content
                    )
                }
                refreshFavorites()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }

            EduMapApiClient.signIn(login, password)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            isAuthLoading = false,
                            authMessage = "Вы вошли как ${session.login}"
                        )
                    }
                    sessionStorage.save(session)
                    refreshFavorites()
                }
                .onFailure { error ->
                    // ✅ error имеет тип Throwable
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authMessage = handleError(error)
                        )
                    }
                }
        }
    }

    fun signUp(login: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.signUp(login, email, password)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        session = session,
                        isAuthLoading = false,
                        authMessage = "Аккаунт создан. Вы вошли как ${session.login}"
                    )
                }
                sessionStorage.save(session)
                refreshFavorites()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun loadUserData() {
        val username = uiState.value.session?.login ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                EduMapApiClient.getUserByUsername(username).getOrThrow()
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        userData = user
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = handleError(error)
                    )
                }
            }
        }
    }
    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val groups = EduMapApiClient.getAllGroups().getOrThrow()
                val fields = EduMapApiClient.getEducationFieldsAll().getOrThrow()
                GroupData(groups = groups, fields = fields)
            }.onSuccess { groupData ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        groupData = groupData
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun loadEducationPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                EduMapApiClient.getEducationPlans().getOrThrow()
            }.onSuccess { plans ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        educationPlans = plans
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun loadDisciplinesData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val groups = EduMapApiClient.getAllGroups().getOrThrow()
                val fields = EduMapApiClient.getEducationFieldsAll().getOrThrow()
                val plans = EduMapApiClient.getEducationPlans().getOrThrow()
                
                GroupData(groups = groups, fields = fields) to plans
            }.onSuccess { (groupData, plans) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        groupData = groupData,
                        educationPlans = plans,
                        educationFields = groupData.fields
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun logout() {
        sessionStorage.clear()
        _uiState.update {
            it.copy(
                session = null,
                favoriteDisciplineIds = emptySet(),
                favoriteTopicIds = emptySet(),
                favoriteDisciplineNotes = emptyMap(),
                favoriteTopicNotes = emptyMap(),
                authMessage = "Вы вышли из аккаунта"
            )
        }
    }

    fun updateLogin(newLogin: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы изменить логин") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.updateLogin(
                    requireFreshSession("Войдите, чтобы изменить логин"),
                    newLogin
                )
            }.onSuccess { updatedSession ->
                _uiState.update {
                    it.copy(
                        session = updatedSession,
                        isAuthLoading = false,
                        authMessage = "Логин изменен на ${updatedSession.login}"
                    )
                }
                sessionStorage.save(updatedSession)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun updatePassword(newPassword: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы изменить пароль") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.updatePassword(
                    requireFreshSession("Войдите, чтобы изменить пароль"),
                    newPassword
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = "Пароль изменен"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun requestPasswordReset(login: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.requestPasswordReset(login)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = "Если такой логин зарегистрирован, письмо для восстановления отправлено на почту аккаунта."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun toggleFavoriteDiscipline(disciplineId: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы добавлять дисциплины в избранное") }
            return
        }

        viewModelScope.launch {
            val wasFavorite = disciplineId in _uiState.value.favoriteDisciplineIds
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                val freshSession = requireFreshSession("Войдите, чтобы добавлять дисциплины в избранное")
                if (wasFavorite) {
                    SupabaseClient.removeFavoriteDiscipline(freshSession, disciplineId)
                } else {
                    SupabaseClient.addFavoriteDiscipline(freshSession, disciplineId)
                }
            }.onSuccess {
                _uiState.update { state ->
                    val updated = if (wasFavorite) {
                        state.favoriteDisciplineIds - disciplineId
                    } else {
                        state.favoriteDisciplineIds + disciplineId
                    }
                    state.copy(
                        favoriteDisciplineIds = updated,
                        favoriteDisciplineNotes = if (wasFavorite) {
                            state.favoriteDisciplineNotes - disciplineId
                        } else {
                            state.favoriteDisciplineNotes + (disciplineId to "")
                        },
                        isAuthLoading = false,
                        authMessage = if (wasFavorite) "Удалено из избранного" else "Добавлено в избранное"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun toggleFavoriteTopic(topicId: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы добавлять темы в избранное") }
            return
        }

        viewModelScope.launch {
            val wasFavorite = topicId in _uiState.value.favoriteTopicIds
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                val freshSession = requireFreshSession("Войдите, чтобы добавлять темы в избранное")
                if (wasFavorite) {
                    SupabaseClient.removeFavoriteTopic(freshSession, topicId)
                } else {
                    SupabaseClient.addFavoriteTopic(freshSession, topicId)
                }
            }.onSuccess {
                _uiState.update { state ->
                    val updated = if (wasFavorite) {
                        state.favoriteTopicIds - topicId
                    } else {
                        state.favoriteTopicIds + topicId
                    }
                    state.copy(
                        favoriteTopicIds = updated,
                        favoriteTopicNotes = if (wasFavorite) {
                            state.favoriteTopicNotes - topicId
                        } else {
                            state.favoriteTopicNotes + (topicId to "")
                        },
                        isAuthLoading = false,
                        authMessage = if (wasFavorite) "Тема удалена из избранного" else "Тема добавлена в избранное"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun updateFavoriteDisciplineNote(disciplineId: String, note: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы сохранять заметки") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.updateFavoriteDisciplineNote(
                    requireFreshSession("Войдите, чтобы сохранять заметки"),
                    disciplineId,
                    note
                )
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        favoriteDisciplineNotes = state.favoriteDisciplineNotes + (disciplineId to note.trim()),
                        isAuthLoading = false,
                        authMessage = "Заметка сохранена"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    fun updateFavoriteTopicNote(topicId: String, note: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(authMessage = "Войдите, чтобы сохранять заметки") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authMessage = null) }
            runCatching {
                SupabaseClient.updateFavoriteTopicNote(
                    requireFreshSession("Войдите, чтобы сохранять заметки"),
                    topicId,
                    note
                )
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        favoriteTopicNotes = state.favoriteTopicNotes + (topicId to note.trim()),
                        isAuthLoading = false,
                        authMessage = "Заметка сохранена"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authMessage = handleError(error)
                    )
                }
            }
        }
    }

    private fun refreshFavorites() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching {
                val freshSession = refreshSessionIfPossible(session)
                SupabaseClient.getFavoriteDisciplineNotes(freshSession.accessToken) to
                    SupabaseClient.getFavoriteTopicNotes(freshSession.accessToken)
            }.onSuccess { (disciplineNotes, topicNotes) ->
                _uiState.update {
                    it.copy(
                        favoriteDisciplineIds = disciplineNotes.keys,
                        favoriteTopicIds = topicNotes.keys,
                        favoriteDisciplineNotes = disciplineNotes,
                        favoriteTopicNotes = topicNotes
                    )
                }
            }.onFailure { error ->
                handleError(error)
            }
        }
    }
}

data class EduMapUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val content: EduMapContent? = null,
    val session: StudentSession? = null,
    val groupData: GroupData? = null,
    val educationPlans: List<EducationPlanResponseDto>? = null,
    val educationFields: List<EducationFieldServiceDto>? = null,
    val userData: UserDto? = null,
    val favoriteDisciplineIds: Set<String> = emptySet(),
    val favoriteTopicIds: Set<String> = emptySet(),
    val favoriteDisciplineNotes: Map<String, String> = emptyMap(),
    val favoriteTopicNotes: Map<String, String> = emptyMap(),
    val isAuthLoading: Boolean = false,
    val authMessage: String? = null
)

data class GroupData(
    val groups: List<GroupServiceDto> = emptyList(),
    val fields: List<EducationFieldServiceDto> = emptyList()
)

data class EduMapContent(
    val programData: ProgramData = ProgramData(),
    val topics: List<Topic> = emptyList(),
    val materials: List<Material> = emptyList()
) {
    val disciplines: List<Discipline>
        get() = programData.disciplines

    fun disciplineById(id: String): Discipline? =
        disciplines.firstOrNull { it.id == id }

    fun topicById(id: String): Topic? =
        topics.firstOrNull { it.id == id }

    fun topicsForDiscipline(disciplineId: String): List<Topic> =
        topics
            .filter { it.disciplineId == disciplineId }
            .sortedWith(compareBy<Topic> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })

    fun materialsForDiscipline(disciplineId: String): List<Material> =
        materials
            .filter { it.disciplineId == disciplineId }
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })

    fun materialsForTopic(topicId: String): List<Material> =
        materials
            .filter { it.topicId == topicId }
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })

    fun materialsForDisciplineAndTopics(disciplineId: String): List<Material> {
        val topicIds = topicsForDiscipline(disciplineId).map { it.id }.toSet()
        return materials
            .filter { it.disciplineId == disciplineId || it.topicId in topicIds }
            .distinctBy { it.id }
            .sortedWith(compareBy<Material> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.title.orEmpty().lowercase() })
    }

    fun search(
        query: String,
        selectedSpecialtyId: String?,
        selectedCourseNumber: Int?,
        selectedSemesterNumber: Int?
    ): SearchData {
        // Курсы с учётом специальности и валидного номера (> 0)
        val allowedCourseIds = programData.courses
            .filter { it.number > 0 }
            .filter { selectedSpecialtyId == null || it.specialtyId == selectedSpecialtyId }
            .filter { selectedCourseNumber == null || it.number == selectedCourseNumber }
            .map { it.id }
            .toSet()

        // Семестры с учётом курсов и валидного номера (> 0)
        val allowedSemesterIds = programData.semesters
            .filter { it.number > 0 }
            .filter { it.courseId in allowedCourseIds }
            .filter { selectedSemesterNumber == null || it.number == selectedSemesterNumber }
            .map { it.id }
            .toSet()

        val allowedDisciplineIds = disciplines
            .filter { it.semesterId == null || it.semesterId in allowedSemesterIds }
            .map { it.id }
            .toSet()

        return SearchData(
            disciplines = disciplines.filter { discipline ->
                (discipline.semesterId == null || discipline.semesterId in allowedSemesterIds) &&
                    (
                        matchesSearch(discipline.name, query) ||
                            matchesSearch(discipline.description, query) ||
                            matchesSearch(discipline.terminology, query) ||
                            matchesSearch(discipline.theory, query) ||
                            matchesSearch(discipline.applicationArea, query)
                        )
            },
            topics = topics.filter { topic ->
                (topic.disciplineId == null || topic.disciplineId in allowedDisciplineIds) &&
                    matchesSearch(topic.name, query)
            }
        )
    }
}
