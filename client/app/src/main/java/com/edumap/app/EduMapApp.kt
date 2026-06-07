package com.edumap.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edumap.app.data.ProgramData
import com.edumap.app.data.SearchData
import com.edumap.app.model.Course
import com.edumap.app.model.Discipline
import com.edumap.app.model.Material
import com.edumap.app.model.Semester
import com.edumap.app.model.Specialty
import com.edumap.app.model.Topic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edumap.app.ui.screens.GroupsScreen
import com.edumap.app.ui.screens.SubjectDetailScreen
import com.edumap.app.ui.screens.SubjectsScreen
import com.edumap.app.ui.screens.DisciplinesScreen
import com.edumap.app.ui.screens.GroupDetailScreen

enum class AppThemeMode(
    val storageValue: String,
    val title: String,
    val description: String
) {
    System(
        storageValue = "system",
        title = "Системная",
        description = "Приложение повторяет тему телефона."
    ),
    Light(
        storageValue = "light",
        title = "Светлая",
        description = "Белый фон для дневного использования."
    ),
    Dark(
        storageValue = "dark",
        title = "Темная",
        description = "Темный фон как в примере."
    );

    companion object {
        fun fromStorage(value: String?): AppThemeMode =
            values().firstOrNull { it.storageValue == value } ?: System
    }
}

private object Routes {
    const val Home = "home"
    const val Disciplines = "disciplines"
    const val Favorites = "favorites"
    const val Profile = "profile"
    const val Program = "program"
    const val Materials = "materials"
    const val Search = "search"
    const val Settings = "settings"
    const val Subjects = "subjects"
    const val Groups = "groups" 
    const val Discipline = "discipline/{id}"
    const val Topic = "topic/{id}"
    const val SubjectDetail = "subject-detail/{planId}/{subjectId}"
    const val GroupDetail = "group-detail/{groupId}"

    fun discipline(id: String) = "discipline/$id"
    fun topic(id: String) = "topic/$id"
    fun subjectDetail(planId: Int, subjectId: Int) = "subject-detail/$planId/$subjectId"
    fun groupDetail(groupId: Int) = "group-detail/$groupId"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.Home, "Главная", Icons.Filled.Home),
    TopLevelDestination(Routes.Disciplines, "Дисциплины", Icons.Filled.School),
    TopLevelDestination(Routes.Groups, "Группы", Icons.Filled.AccountTree),
    TopLevelDestination(Routes.Subjects, "Предметы", Icons.AutoMirrored.Filled.MenuBook),
    TopLevelDestination(Routes.Profile, "Профиль", Icons.Filled.Person)
)

@Composable
fun EduMapApp(
    themeMode: AppThemeMode = AppThemeMode.System,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    viewModel: EduMapViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.authMessage) {
        uiState.authMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAuthMessage()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }
    LaunchedEffect(Unit) {
        viewModel.loadEducationPlans()
    }
    LaunchedEffect(Unit) {
        viewModel.loadDisciplinesData()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = Routes.Home) {
            composable(Routes.Home) {
                AppScaffold(navController = navController, title = "EduMap", uiState = uiState, snackbarHostState = snackbarHostState) {
                    HomeScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onDisciplinesClick = { navController.navigateSingleTop(Routes.Disciplines) },
                        onGroupsClick = { navController.navigateSingleTop(Routes.Groups) },    
                        onSubjectsClick = { navController.navigateSingleTop(Routes.Subjects) },
                        onProfileClick = { navController.navigateSingleTop(Routes.Profile) }  
                    )
                }
            }
            composable(Routes.Disciplines) {
                AppScaffold(
                    navController = navController,
                    title = "Дисциплины",
                    uiState = uiState,
                    snackbarHostState = snackbarHostState
                ) {
                    DisciplinesScreen(
                        uiState = uiState,
                        onRetry = viewModel::loadDisciplinesData,
                        onGroupClick = { group ->
                            navController.navigate(Routes.groupDetail(group.id))
                        }
                    )
                }
            }
            composable(Routes.GroupDetail) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: return@composable
                val group = uiState.groupData?.groups?.firstOrNull { it.id == groupId }
                
                AppScaffold(
                    navController = navController,
                    title = "Группа",
                    showBack = true,  // ← Всегда показываем кнопку назад!
                    uiState = uiState,
                    snackbarHostState = snackbarHostState
                ) {
                    if (group != null) {
                        GroupDetailScreen(
                            group = group,
                            uiState = uiState,
                            onRetry = viewModel::loadDisciplinesData,
                            onBackClick = { navController.popBackStack() },
                            onSubjectClick = { plan, subject ->
                                navController.navigate(Routes.subjectDetail(plan.id, subject.id))
                            }
                        )
                    } else {
                        // Показываем ошибку внутри Scaffold
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Группа не найдена",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Возможно, группа была удалена или еще не добавлена.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Вернуться к списку")
                                }
                            }
                        }
                    }
                }
            }
            composable(Routes.Favorites) {
                AppScaffold(navController = navController, title = "Избранное", uiState = uiState, snackbarHostState = snackbarHostState) {
                    FavoritesScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onAuthClick = { navController.navigateSingleTop(Routes.Profile) },
                        onDisciplineClick = { navController.navigate(Routes.discipline(it.id)) },
                        onDisciplineFavoriteClick = viewModel::toggleFavoriteDiscipline,
                        onDisciplineNoteSave = viewModel::updateFavoriteDisciplineNote,
                        onTopicClick = { navController.navigate(Routes.topic(it.id)) },
                        onTopicFavoriteClick = viewModel::toggleFavoriteTopic,
                        onTopicNoteSave = viewModel::updateFavoriteTopicNote
                    )
                }
            }
            composable(Routes.Groups) {
                AppScaffold(
                    navController = navController, 
                    title = "Группы", 
                    uiState = uiState, 
                    snackbarHostState = snackbarHostState
                ) {
                    GroupsScreen(
                        uiState = uiState,
                        onRetry = viewModel::loadGroups,
                        onGroupClick = { /* пока заглушка */ }
                    )
                }
            }
            composable(Routes.Profile) {
                AppScaffold(navController = navController, title = "Личный кабинет", uiState = uiState, snackbarHostState = snackbarHostState) {
                    ProfileScreen(
                        uiState = uiState,
                        onSignIn = viewModel::signIn,
                        onSignUp = viewModel::signUp,
                        onUpdateLogin = viewModel::updateLogin,
                        onUpdatePassword = viewModel::updatePassword,
                        onPasswordResetRequest = viewModel::requestPasswordReset,
                        onLogout = viewModel::logout,
                        onLoadData = viewModel::loadUserData  // ← НОВЫЙ ПАРАМЕТР
                    )
                }
            }
            composable(Routes.Search) {
                AppScaffold(navController = navController, title = "Поиск", uiState = uiState, snackbarHostState = snackbarHostState) {
                    EnhancedSearchScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onDisciplineClick = { navController.navigate(Routes.discipline(it.id)) },
                        onTopicClick = { navController.navigate(Routes.topic(it.id)) }
                    )
                }
            }
            composable(Routes.Program) {
                AppScaffold(navController = navController, title = "Учебная программа", showBack = true, uiState = uiState, snackbarHostState = snackbarHostState) {
                    ProgramScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onDisciplineClick = { navController.navigate(Routes.discipline(it.id)) }
                    )
                }
            }
            composable(Routes.Subjects) {
                AppScaffold(
                    navController = navController, 
                    title = "Предметы", 
                    uiState = uiState, 
                    snackbarHostState = snackbarHostState
                ) {
                    SubjectsScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onSubjectClick = { plan, subject ->
                            navController.navigate(Routes.subjectDetail(plan.id, subject.id))
                        }
                    )
                }
            }
            composable(Routes.SubjectDetail) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId")?.toIntOrNull() ?: return@composable
                val subjectId = backStackEntry.arguments?.getString("subjectId")?.toIntOrNull() ?: return@composable
                
                val plan = uiState.educationPlans?.firstOrNull { it.id == planId }
                val subject = plan?.educationPlan?.educationSubjects?.firstOrNull { it.id == subjectId }
                
                if (plan != null && subject != null) {
                    SubjectDetailScreen(
                        plan = plan,
                        subject = subject,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Предмет не найден")
                    }
                }
            }
            composable(Routes.Discipline) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                AppScaffold(navController = navController, title = "Дисциплина", showBack = true, uiState = uiState, snackbarHostState = snackbarHostState) {
                    DisciplineDetailScreen(
                        disciplineId = id,
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onAuthClick = { navController.navigateSingleTop(Routes.Profile) },
                        onTopicClick = { navController.navigate(Routes.topic(it.id)) },
                        onFavoriteClick = viewModel::toggleFavoriteDiscipline
                    )
                }
            }
            composable(Routes.Topic) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                AppScaffold(navController = navController, title = "Тема", showBack = true, uiState = uiState, snackbarHostState = snackbarHostState) {
                    TopicDetailScreen(
                        topicId = id,
                        uiState = uiState,
                        onRetry = viewModel::refresh,
                        onAuthClick = { navController.navigateSingleTop(Routes.Profile) },
                        onFavoriteClick = viewModel::toggleFavoriteTopic
                    )
                }
            }
            composable(Routes.Materials) {
                AppScaffold(navController = navController, title = "Материалы", showBack = true, uiState = uiState, snackbarHostState = snackbarHostState) {
                    MaterialsScreen(
                        uiState = uiState,
                        onRetry = viewModel::refresh
                    )
                }
            }
            composable(Routes.Settings) {
                AppScaffold(
                    navController = navController,
                    title = "Настройки",
                    showBack = true,
                    uiState = uiState,
                    snackbarHostState = snackbarHostState
                ) {
                    SettingsScreen(
                        uiState = uiState,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        onProfileClick = {
                            navController.popBackStack()
                            navController.navigateSingleTop(Routes.Profile)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    navController: NavHostController,
    title: String,
    showBack: Boolean = false,
    uiState: EduMapUiState,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppTopTitle(
                        title = title,
                        login = uiState.session?.login
                    )
                },
                navigationIcon = {
                },
                actions = {
                    when {
                        showBack -> {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Вернуться назад"
                                )
                            }
                        }

                        !showBack -> {
                            IconButton(
                                onClick = {
                                    navController.navigate(Routes.Settings) {
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Открыть настройки"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (!showBack) {
                EduMapNavigationBar(navController = navController)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .then(
                        if (showBack) {
                            Modifier
                        } else {
                            Modifier.topLevelSwipeNavigation(navController)
                        }
                    )
            ) {
                content()
            }
        }
    )
}

private fun Modifier.topLevelSwipeNavigation(navController: NavHostController): Modifier =
    pointerInput(navController) {
        var dragOffset = 0f
        detectHorizontalDragGestures(
            onDragStart = { dragOffset = 0f },
            onHorizontalDrag = { change, dragAmount ->
                dragOffset += dragAmount
                change.consume()
            },
            onDragCancel = { dragOffset = 0f },
            onDragEnd = {
                when {
                    dragOffset <= -SWIPE_NAVIGATION_THRESHOLD -> {
                        navController.navigateAdjacentTopLevel(step = 1)
                    }

                    dragOffset >= SWIPE_NAVIGATION_THRESHOLD -> {
                        navController.navigateAdjacentTopLevel(step = -1)
                    }
                }
                dragOffset = 0f
            }
        )
    }

private fun NavHostController.navigateAdjacentTopLevel(step: Int) {
    val currentRoute = currentBackStackEntry?.destination?.route ?: Routes.Home
    val currentIndex = topLevelDestinations.indexOfFirst { it.route == currentRoute }
    if (currentIndex == -1) return

    val nextDestination = topLevelDestinations.getOrNull(currentIndex + step) ?: return
    navigateSingleTop(nextDestination.route)
}

private const val SWIPE_NAVIGATION_THRESHOLD = 120f

@Composable
private fun AppTopTitle(
    title: String,
    login: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.edumap_splash_icon),
                contentDescription = "EduMap",
                modifier = Modifier
                    .padding(6.dp)
                    .size(34.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = login?.let { "Профиль: $it" } ?: "Материалы доступны без входа",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EduMapNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { navController.navigateSingleTop(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(Routes.Home) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun HomeScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onDisciplinesClick: () -> Unit,
    onGroupsClick: () -> Unit,        // ← Добавлено
    onSubjectsClick: () -> Unit,       // ← Добавлено
    onProfileClick: () -> Unit         // ← Добавлено (вместо onProgramClick, onMaterialsClick, onSearchClick)
) {
    val content = uiState.content
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка с названием приложения
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EduMap",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Учебный план, дисциплины, темы и полезные материалы колледжа в одном удобном месте.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
        
        // Состояние загрузки / ошибки
        when {
            uiState.isLoading && content == null -> {
                InfoStateCard(
                    title = "Загружаю данные",
                    message = "Подождите немного..."
                )
            }
            uiState.errorMessage != null && content == null -> {
                RetryStateCard(
                    title = "Нет доступа к данным",
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
        }
        
        // Заголовок сетки
        Text(
            text = "Разделы",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Сетка — горизонтальные карточки в полную ширину
        val tiles = buildList {
            add(HomeTile(
                title = "Дисциплины",
                subtitle = "Все предметы с описанием, терминологией и материалами",
                icon = Icons.Filled.School,
                onClick = onDisciplinesClick
            ))
            add(HomeTile(
                title = "Группы",
                subtitle = "Учебные группы по направлениям и курсам",
                icon = Icons.Filled.AccountTree,
                onClick = onGroupsClick
            ))
            add(HomeTile(
                title = "Предметы",
                subtitle = "Учебные планы и предметы по специальностям",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onSubjectsClick
            ))
            add(HomeTile(
                title = "Профиль",
                subtitle = "Личный кабинет, избранное и настройки",
                icon = Icons.Filled.Person,
                onClick = onProfileClick
            ))
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tiles) { tile ->
                HomeTileCard(tile = tile)
            }
        }
    }
}

private data class HomeTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun HomeTileCard(
    tile: HomeTile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = tile.onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка в зелёном квадрате
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.title,
                    modifier = Modifier.padding(16.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Текст
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Стрелка
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoStateCard(
    title: String,
    message: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RetryStateCard(
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun DisciplinesScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onDisciplineClick: (Discipline) -> Unit,
    onAuthClick: () -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    var queryField by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val query = queryField.text

    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        val disciplines = content.disciplines
            .filter { discipline ->
                matchesSearch(discipline.name, query) ||
                    matchesSearch(discipline.description, query) ||
                    matchesSearch(discipline.terminology, query) ||
                    matchesSearch(discipline.theory, query) ||
                    matchesSearch(discipline.applicationArea, query)
            }
            .sortedWith(compareBy<Discipline> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Список всех дисциплин",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ищите предмет по названию, терминам или теории. Откройте дисциплину, чтобы увидеть темы и полезные источники.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                OutlinedTextField(
                    value = queryField,
                    onValueChange = { queryField = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск по названию") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    )
                )
            }

            item {
                Text(
                    text = if (query.isBlank()) {
                        "Всего дисциплин: ${disciplines.size}"
                    } else {
                        "Найдено дисциплин: ${disciplines.size}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (disciplines.isEmpty()) {
                item {
                    EmptyInline(
                        if (query.isBlank()) "Дисциплины пока не добавлены."
                        else "По этому запросу дисциплины не найдены."
                    )
                }
            } else {
                items(disciplines, key = { it.id }) { discipline ->
                    DisciplineListCard(
                        discipline = discipline,
                        topicCount = content.topicsForDiscipline(discipline.id).size,
                        materialCount = content.materialsForDisciplineAndTopics(discipline.id).size,
                        onClick = { onDisciplineClick(discipline) },
                        isSignedIn = uiState.session != null,
                        isFavorite = discipline.id in uiState.favoriteDisciplineIds,
                        isBusy = uiState.isAuthLoading,
                        onAuthClick = onAuthClick,
                        onFavoriteClick = { onFavoriteClick(discipline.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisciplineListCard(
    discipline: Discipline,
    topicCount: Int,
    materialCount: Int,
    onClick: () -> Unit,
    isSignedIn: Boolean,
    isFavorite: Boolean,
    isBusy: Boolean,
    onAuthClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = discipline.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!discipline.description.isNullOrBlank()) {
                Text(
                    text = discipline.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CountBadge("Тем", topicCount)
                }
                item {
                    CountBadge("Материалов", materialCount)
                }
            }
            OutlinedButton(
                onClick = if (isSignedIn) onFavoriteClick else onAuthClick,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        !isSignedIn -> "Войти, чтобы добавить в избранное"
                        isFavorite -> "Убрать из избранного"
                        else -> "Добавить в избранное"
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoritesScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onAuthClick: () -> Unit,
    onDisciplineClick: (Discipline) -> Unit,
    onDisciplineFavoriteClick: (String) -> Unit,
    onDisciplineNoteSave: (String, String) -> Unit,
    onTopicClick: (Topic) -> Unit,
    onTopicFavoriteClick: (String) -> Unit,
    onTopicNoteSave: (String, String) -> Unit
) {
    if (uiState.session == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Избранное",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Войдите, чтобы сохранять выбранные дисциплины и темы в личном списке.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            item {
                Button(
                    onClick = onAuthClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Перейти к входу")
                }
            }
        }
        return
    }

    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        val favoriteDisciplines = content.disciplines
            .filter { it.id in uiState.favoriteDisciplineIds }
            .sortedWith(compareBy<Discipline> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
        val favoriteTopics = content.topics
            .filter { it.id in uiState.favoriteTopicIds }
            .sortedWith(compareBy<Topic> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FavoritesSummaryCard(
                    disciplineCount = favoriteDisciplines.size,
                    topicCount = favoriteTopics.size
                )
            }

            if (favoriteDisciplines.isEmpty() && favoriteTopics.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Здесь пока пусто",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Откройте список дисциплин или страницу темы и нажмите «Добавить в избранное». После этого здесь появятся ваши заметки.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            if (favoriteDisciplines.isNotEmpty()) {
                item { SectionTitle("Дисциплины") }
                items(favoriteDisciplines, key = { it.id }) { discipline ->
                    FavoriteDisciplineCard(
                        discipline = discipline,
                        topicCount = content.topicsForDiscipline(discipline.id).size,
                        materialCount = content.materialsForDisciplineAndTopics(discipline.id).size,
                        note = uiState.favoriteDisciplineNotes[discipline.id].orEmpty(),
                        isBusy = uiState.isAuthLoading,
                        onClick = { onDisciplineClick(discipline) },
                        onFavoriteClick = { onDisciplineFavoriteClick(discipline.id) },
                        onNoteSave = { note -> onDisciplineNoteSave(discipline.id, note) }
                    )
                }
            }

            if (favoriteTopics.isNotEmpty()) {
                item { SectionTitle("Темы") }
                items(favoriteTopics, key = { it.id }) { topic ->
                    FavoriteTopicCard(
                        topic = topic,
                        discipline = topic.disciplineId?.let { content.disciplineById(it) },
                        materialCount = content.materialsForTopic(topic.id).size,
                        note = uiState.favoriteTopicNotes[topic.id].orEmpty(),
                        isBusy = uiState.isAuthLoading,
                        onClick = { onTopicClick(topic) },
                        onFavoriteClick = { onTopicFavoriteClick(topic.id) },
                        onNoteSave = { note -> onTopicNoteSave(topic.id, note) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesSummaryCard(
    disciplineCount: Int,
    topicCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Избранное",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ваши сохраненные дисциплины, темы и личные заметки для повторения.",
                style = MaterialTheme.typography.bodyLarge
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CountBadge("Дисциплин", disciplineCount)
                }
                item {
                    CountBadge("Тем", topicCount)
                }
                }
            }
        }
    }

@Composable
private fun FavoriteDisciplineCard(
    discipline: Discipline,
    topicCount: Int,
    materialCount: Int,
    note: String,
    isBusy: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNoteSave: (String) -> Unit
) {
    var draftNote by remember(discipline.id, note) { mutableStateOf(note) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = discipline.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!discipline.description.isNullOrBlank()) {
                Text(
                    text = discipline.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CountBadge("Тем", topicCount)
                }
                item {
                    CountBadge("Материалов", materialCount)
                }
            }
            OutlinedTextField(
                value = draftNote,
                onValueChange = { draftNote = it.take(500) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Личная заметка") },
                minLines = 2,
                maxLines = 4
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNoteSave(draftNote) },
                    enabled = !isBusy && draftNote.trim() != note,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
                OutlinedButton(
                    onClick = onFavoriteClick,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Убрать")
                }
            }
        }
    }
}

@Composable
private fun FavoriteTopicCard(
    topic: Topic,
    discipline: Discipline?,
    materialCount: Int,
    note: String,
    isBusy: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNoteSave: (String) -> Unit
) {
    var draftNote by remember(topic.id, note) { mutableStateOf(note) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = topic.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (discipline != null) {
                Text(
                    text = discipline.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CountBadge("Материалов", materialCount)
                }
            }
            OutlinedTextField(
                value = draftNote,
                onValueChange = { draftNote = it.take(500) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Личная заметка") },
                minLines = 2,
                maxLines = 4
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNoteSave(draftNote) },
                    enabled = !isBusy && draftNote.trim() != note,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
                OutlinedButton(
                    onClick = onFavoriteClick,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Убрать")
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    uiState: EduMapUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onUpdateLogin: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onPasswordResetRequest: (String) -> Unit,
    onLogout: () -> Unit,
    onLoadData: () -> Unit  // ← НОВЫЙ ПАРАМЕТР
) {
    // Загружаем данные при входе
    LaunchedEffect(uiState.session?.login) {
        if (uiState.session != null) {
            onLoadData()
        }
    }
    
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recoveryLogin by remember { mutableStateOf("") }
    var isRegistration by remember { mutableStateOf(false) }
    val currentLogin = uiState.session?.login.orEmpty()
    var newLogin by remember(currentLogin) { mutableStateOf(currentLogin) }
    var newPassword by remember { mutableStateOf("") }
    
    val submitAuth = {
        if (isRegistration) onSignUp(login, email, password) else onSignIn(login, password)
    }
    
    val submitLoginChange = {
        if (newLogin.trim() != currentLogin) {
            onUpdateLogin(newLogin)
        }
    }
    
    val submitPasswordChange = {
        if (newPassword.length in 6..8) {
            onUpdatePassword(newPassword)
            newPassword = ""
        }
    }
    
    val submitPasswordRecovery = {
        val preparedLogin = recoveryLogin.ifBlank { login }
        if (preparedLogin.isNotBlank()) {
            onPasswordResetRequest(preparedLogin)
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.session != null) {
            // Блок "Вы вошли как"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Вы вошли как ${uiState.session.login}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ваши сохраненные дисциплины, темы и заметки доступны на вкладке «Избранное».",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Выйти из аккаунта")
                        }
                    }
                }
            }
            
            // Данные пользователя из API
            if (uiState.userData != null) {
                val user = uiState.userData
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Информация о пользователе",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            // ФИО
                            Text(
                                text = "${user.lastName} ${user.firstName} ${user.fatherName.orEmpty()}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            HorizontalDivider()
                            
                            // Email
                            if (!user.email.isNullOrBlank()) {
                                InfoRow("Email", user.email)
                            }
                            
                            // Роль
                            InfoRow(
                                "Роль",
                                when (user.role) {
                                    "STUDENT" -> "Студент"
                                    "TEACHER" -> "Преподаватель"
                                    "ADMIN" -> "Администратор"
                                    else -> user.role
                                }
                            )
                            
                            // Группа
                            if (user.group != null) {
                                HorizontalDivider()
                                Text(
                                    text = "Группа",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                InfoRow("Номер", user.group.name)
                                if (user.group.course != null && user.group.course > 0) {
                                    InfoRow("Курс", "${user.group.course}")
                                }
                            }
                        }
                    }
                }
            } else if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            
            return@LazyColumn
        }
        
        // Если не авторизован - показываем форму входа (как было)
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isRegistration) "Создать аккаунт" else "Войти в аккаунт",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isRegistration) {
                            "Аккаунт нужен для избранного и личных заметок. Учебные материалы остаются доступными без регистрации."
                        } else {
                            "Введите логин и пароль, чтобы открыть избранное и заметки."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Логин") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    if (isRegistration) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Почта для восстановления") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.take(8) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAuth() })
                    )
                    Text(
                        text = if (isRegistration) {
                            "Логин нужен для входа, почта нужна для восстановления пароля. Пароль: от 6 до 8 символов."
                        } else {
                            "Войдите по логину и паролю. Материалы доступны и без входа, аккаунт нужен для избранного."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = submitAuth,
                        enabled = !uiState.isAuthLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegistration) "Зарегистрироваться" else "Войти")
                    }
                    TextButton(
                        onClick = {
                            isRegistration = !isRegistration
                            password = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isRegistration) "Уже есть аккаунт? Войти"
                            else "Нет аккаунта? Зарегистрироваться"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProgramScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onDisciplineClick: (Discipline) -> Unit
) {
    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        ProgramContent(
            data = content.programData,
            onDisciplineClick = onDisciplineClick
        )
    }
}

@Composable
private fun ProgramContent(
    data: ProgramData,
    onDisciplineClick: (Discipline) -> Unit
) {
    var selectedSpecialtyId by remember(data.specialties) { mutableStateOf<String?>(null) }
    var selectedCourseNumber by remember(data.courses) { mutableStateOf<Int?>(null) }
    var selectedSemesterNumber by remember(data.semesters) { mutableStateOf<Int?>(null) }

    val coursesBySpecialty = data.courses.groupBy { it.specialtyId }
    val semestersByCourse = data.semesters.groupBy { it.courseId }
    val disciplinesBySemester = data.disciplines.groupBy { it.semesterId }
    val visibleCourseIds = data.visibleCourseIds(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber
    )
    val visibleSemesterIds = data.visibleSemesterIds(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber,
        selectedSemesterNumber = selectedSemesterNumber
    )
    val selectedSpecialties = if (selectedSpecialtyId == null) {
        data.specialties.filter { specialty ->
            val specialtyCourseIds = coursesBySpecialty[specialty.id].orEmpty()
                .filter { it.id in visibleCourseIds }
                .map { it.id }
                .toSet()
            specialtyCourseIds.isNotEmpty() && (
                selectedSemesterNumber == null ||
                    data.semesters.any { it.courseId in specialtyCourseIds && it.id in visibleSemesterIds }
                )
        }
    } else {
        data.specialties.filter { it.id == selectedSpecialtyId }
    }
    val availableCourseNumbers = data.availableCourseNumbers(selectedSpecialtyId)
    val availableSemesterNumbers = data.availableSemesterNumbers(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber
    )
    val activeFilters = data.filterSummaryItems(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber,
        selectedSemesterNumber = selectedSemesterNumber
    )
    val visibleDisciplineCount = if (data.specialties.isEmpty()) {
        data.disciplines.count { discipline ->
            discipline.semesterId == null || discipline.semesterId in visibleSemesterIds
        }
    } else {
        data.disciplines.count { discipline ->
            discipline.semesterId == null || discipline.semesterId in visibleSemesterIds
        }
    }

    if (data.specialties.isEmpty() && data.courses.isEmpty() && data.semesters.isEmpty() && data.disciplines.isEmpty()) {
        EmptyState("В базе пока нет учебной программы.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProgramOverviewCard(data = data)
        }

        item {
            ProgramFilters(
                data = data,
                availableCourseNumbers = availableCourseNumbers,
                availableSemesterNumbers = availableSemesterNumbers,
                selectedSpecialtyId = selectedSpecialtyId,
                selectedCourseNumber = selectedCourseNumber,
                selectedSemesterNumber = selectedSemesterNumber,
                onSpecialtySelected = {
                    selectedSpecialtyId = it
                    selectedCourseNumber = null
                    selectedSemesterNumber = null
                },
                onCourseSelected = {
                    selectedCourseNumber = it
                    selectedSemesterNumber = null
                },
                onSemesterSelected = {
                    selectedSemesterNumber = it
                }
            )
        }

        if (activeFilters.isNotEmpty()) {
            item {
                ActiveFiltersCard(
                    title = "Выбранные фильтры",
                    description = "Учебная программа уже сужена по нужным параметрам.",
                    items = activeFilters,
                    onReset = {
                        selectedSpecialtyId = null
                        selectedCourseNumber = null
                        selectedSemesterNumber = null
                    }
                )
            }
        }

        item {
            ProgramResultsCard(
                specialtyCount = selectedSpecialties.size,
                courseCount = if (selectedCourseNumber == null) availableCourseNumbers.size else 1,
                semesterCount = visibleSemesterIds.size,
                disciplineCount = visibleDisciplineCount
            )
        }

        if (data.specialties.isEmpty()) {
            item {
                SectionTitle("Дисциплины")
            }
            items(
                data.disciplines.filter { it.semesterId == null || it.semesterId in visibleSemesterIds },
                key = { it.id }
            ) { discipline ->
                DisciplineRow(discipline = discipline, onClick = { onDisciplineClick(discipline) })
            }
            return@LazyColumn
        }

        if (visibleDisciplineCount == 0) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "По выбранным фильтрам дисциплины пока не найдены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Измените специальность, курс или семестр, либо сбросьте фильтры полностью.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = {
                                selectedSpecialtyId = null
                                selectedCourseNumber = null
                                selectedSemesterNumber = null
                            }
                        ) {
                            Text("Сбросить фильтры")
                        }
                    }
                }
            }
            return@LazyColumn
        }

        selectedSpecialties.forEach { specialty ->
            item(key = specialty.id) {
                SectionTitle(specialty.name)
            }

            val courses = coursesBySpecialty[specialty.id].orEmpty()
                .filter { selectedCourseNumber == null || it.number == selectedCourseNumber }
                .sortedBy { it.number }
            courses.forEach { course ->
                item(key = course.id) {
                    Text(
                        text = "${course.number} курс",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val semesters = semestersByCourse[course.id].orEmpty()
                    .filter { selectedSemesterNumber == null || it.number == selectedSemesterNumber }
                    .sortedBy { it.number }
                semesters.forEach { semester ->
                    item(key = semester.id) {
                        SemesterBlock(
                            semester = semester,
                            disciplines = disciplinesBySemester[semester.id].orEmpty(),
                            onDisciplineClick = onDisciplineClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramFilters(
    data: ProgramData,
    availableCourseNumbers: List<Int>,
    availableSemesterNumbers: List<Int>,
    selectedSpecialtyId: String?,
    selectedCourseNumber: Int?,
    selectedSemesterNumber: Int?,
    onSpecialtySelected: (String?) -> Unit,
    onCourseSelected: (Int?) -> Unit,
    onSemesterSelected: (Int?) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Фильтры учебной программы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Выберите специальность, курс и семестр, чтобы быстро найти нужную часть программы.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterGroup(title = "Специальность") {
                    FilterChipRow {
                        SelectableFilterChip(
                            text = "Все",
                            selected = selectedSpecialtyId == null,
                            onClick = { onSpecialtySelected(null) }
                        )
                        data.specialties.forEach { specialty ->
                            SelectableFilterChip(
                                text = specialty.name,
                                selected = selectedSpecialtyId == specialty.id,
                                onClick = { onSpecialtySelected(specialty.id) }
                            )
                        }
                    }
                }

                FilterGroup(title = "Курс") {
                    FilterChipRow {
                        SelectableFilterChip(
                            text = "Все",
                            selected = selectedCourseNumber == null,
                            onClick = { onCourseSelected(null) }
                        )
                        availableCourseNumbers.forEach { courseNumber ->
                            SelectableFilterChip(
                                text = "$courseNumber курс",
                                selected = selectedCourseNumber == courseNumber,
                                onClick = { onCourseSelected(courseNumber) }
                            )
                        }
                    }
                }

                FilterGroup(title = "Семестр") {
                    FilterChipRow {
                        SelectableFilterChip(
                            text = "Все",
                            selected = selectedSemesterNumber == null,
                            onClick = { onSemesterSelected(null) }
                        )
                        availableSemesterNumbers.forEach { semesterNumber ->
                            SelectableFilterChip(
                                text = "$semesterNumber семестр",
                                selected = selectedSemesterNumber == semesterNumber,
                                onClick = { onSemesterSelected(semesterNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramOverviewCard(data: ProgramData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Учебная программа",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Здесь можно открыть структуру по специальности, курсу и семестру, а затем перейти в нужную дисциплину.",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { CountBadge("Специальностей", data.specialties.size) }
                item { CountBadge("Курсов", data.courses.size) }
                item { CountBadge("Семестров", data.semesters.size) }
                item { CountBadge("Дисциплин", data.disciplines.size) }
            }
        }
    }
}

@Composable
private fun ProgramResultsCard(
    specialtyCount: Int,
    courseCount: Int,
    semesterCount: Int,
    disciplineCount: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Что сейчас показано",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Сводка помогает быстро понять, какая часть учебной программы попала в выборку.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { CountBadge("Специальностей", specialtyCount) }
                item { CountBadge("Курсов", courseCount) }
                item { CountBadge("Семестров", semesterCount) }
                item { CountBadge("Дисциплин", disciplineCount) }
            }
        }
    }
}

@Composable
private fun ActiveFiltersCard(
    title: String,
    description: String,
    items: List<Pair<String, String>>,
    onReset: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            items.forEach { (label, value) ->
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onReset) {
                Text("Сбросить")
            }
        }
    }
}

@Composable
private fun SearchSummaryCard(
    query: String,
    disciplinesCount: Int,
    topicsCount: Int,
    activeFilters: List<Pair<String, String>>,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Поиск и фильтрация",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Поиск работает по названию дисциплины или темы, а фильтры помогают сузить результат по учебной программе.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (query.isNotBlank()) {
                Text(
                    text = "Запрос: $query",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { CountBadge("Дисциплин", disciplinesCount) }
                    item { CountBadge("Тем", topicsCount) }
                }
            }
            if (activeFilters.isNotEmpty()) {
                activeFilters.forEach { (label, value) ->
                    SelectableFilterChip(
                        text = "$label: $value",
                        selected = true,
                        onClick = {}
                    )
                }
            }
            if (query.isNotBlank() || activeFilters.isNotEmpty()) {
                TextButton(onClick = onReset) {
                    Text("Сбросить поиск и фильтры")
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun FilterChipRow(content: @Composable () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SelectableFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = trimDisplayText(text, 34),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun SemesterBlock(
    semester: Semester,
    disciplines: List<Discipline>,
    onDisciplineClick: (Discipline) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${semester.number} семестр",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (disciplines.isEmpty()) {
            Text("Дисциплины пока не добавлены.", style = MaterialTheme.typography.bodyMedium)
        } else {
            disciplines
                .sortedWith(compareBy<Discipline> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
                .forEach { discipline ->
                DisciplineRow(discipline = discipline, onClick = { onDisciplineClick(discipline) })
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun DisciplineRow(
    discipline: Discipline,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = discipline.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!discipline.description.isNullOrBlank()) {
                    Text(
                        text = discipline.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Открыть дисциплину",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть дисциплину",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DisciplineDetailScreen(
    disciplineId: String,
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onAuthClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        val discipline = content.disciplineById(disciplineId)
        if (discipline == null) {
            ErrorState("Дисциплина не найдена")
            return@ContentGate
        }

        DisciplineDetailContent(
            details = DisciplineDetails(
                discipline = discipline,
                topics = content.topicsForDiscipline(discipline.id),
                materials = content.materialsForDisciplineAndTopics(discipline.id),
                courseNumber = discipline.semesterId?.let { semId ->
                    val semester = content.programData.semesters.firstOrNull { it.id == semId }
                    val course = semester?.courseId?.let { cId ->
                        content.programData.courses.firstOrNull { it.id == cId }
                    }
                    course?.number
                },
                semesterNumber = discipline.semesterId?.let { semId ->
                    content.programData.semesters.firstOrNull { it.id == semId }?.number
                },
                specialtyName = discipline.semesterId?.let { semId ->
                    val semester = content.programData.semesters.firstOrNull { it.id == semId }
                    val course = semester?.courseId?.let { cId ->
                        content.programData.courses.firstOrNull { it.id == cId }
                    }
                    course?.specialtyId?.let { spId ->
                        content.programData.specialties.firstOrNull { it.id == spId }?.name
                    }
                }
            ),
            isSignedIn = uiState.session != null,
            isFavorite = discipline.id in uiState.favoriteDisciplineIds,
            isBusy = uiState.isAuthLoading,
            onAuthClick = onAuthClick,
            onFavoriteClick = { onFavoriteClick(discipline.id) },
            onTopicClick = onTopicClick
        )
    }
}

@Composable
private fun DisciplineDetailContent(
    details: DisciplineDetails,
    isSignedIn: Boolean,
    isFavorite: Boolean,
    isBusy: Boolean,
    onAuthClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onTopicClick: (Topic) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DisciplineHeaderCard(
                details = details,
                isSignedIn = isSignedIn,
                isFavorite = isFavorite,
                isBusy = isBusy,
                onAuthClick = onAuthClick,
                onFavoriteClick = onFavoriteClick
            )
        }

        val hasMainContent = !details.discipline.terminology.isNullOrBlank() ||
            !details.discipline.theory.isNullOrBlank() ||
            !details.discipline.applicationArea.isNullOrBlank()

        if (!hasMainContent) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Содержание скоро появится",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Преподаватель может добавить терминологию, теорию и применение дисциплины через админ-панель.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (!details.discipline.terminology.isNullOrBlank()) {
            item {
                DisciplineInfoSection(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Терминология",
                    text = details.discipline.terminology
                )
            }
        }

        if (!details.discipline.theory.isNullOrBlank()) {
            item {
                DisciplineInfoSection(
                    icon = Icons.AutoMirrored.Filled.Article,
                    title = "Теория",
                    text = details.discipline.theory
                )
            }
        }

        if (!details.discipline.applicationArea.isNullOrBlank()) {
            item {
                DisciplineInfoSection(
                    icon = Icons.Filled.AccountTree,
                    title = "Для чего и где применяется",
                    text = details.discipline.applicationArea
                )
            }
        }

        item {
            SectionTitle("Темы, которые важно изучить")
        }

        if (details.topics.isEmpty()) {
            item { EmptyInline("Темы пока не добавлены.") }
        } else {
            items(details.topics, key = { it.id }) { topic ->
                TopicRow(topic = topic, onClick = { onTopicClick(topic) })
            }
        }

        item {
            SectionTitle("База знаний")
        }

        if (details.materials.isEmpty()) {
            item { EmptyInline("Материалы пока не добавлены.") }
        } else {
            items(details.materials, key = { it.id }) { material ->
                MaterialCard(material)
            }
        }
    }
}

@Composable
private fun DisciplineHeaderCard(
    details: DisciplineDetails,
    isSignedIn: Boolean,
    isFavorite: Boolean,
    isBusy: Boolean,
    onAuthClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Название слева
            Text(
                text = details.discipline.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Специальность + курс + семестр в одну строку
            val metaParts = buildList {
                if (details.specialtyName != null) add(details.specialtyName)
                if (details.courseNumber != null) add("${details.courseNumber} курс")
                if (details.semesterNumber != null) add("${details.semesterNumber} семестр")
            }
            if (metaParts.isNotEmpty()) {
                Text(
                    text = metaParts.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Описание — выделено как отдельный блок
            if (!details.discipline.description.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = details.discipline.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Счётчики в столбик
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CountBadgeRow("Тем", details.topics.size)
                CountBadgeRow("Материалов", details.materials.size)
            }

            // Кнопка избранного
            Button(
                onClick = if (isSignedIn) onFavoriteClick else onAuthClick,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        !isSignedIn -> "Войти, чтобы добавить дисциплину в избранное"
                        isFavorite -> "Убрать дисциплину из избранного"
                        else -> "Добавить дисциплину в избранное"
                    }
                )
            }
        }
    }
}

@Composable
private fun DisciplineInfoSection(
    icon: ImageVector,
    title: String,
    text: String
) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Описание:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TopicRow(
    topic: Topic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = topic.orderIndex?.toString() ?: "-",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Открыть тему",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть тему",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopicDetailScreen(
    topicId: String,
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onAuthClick: () -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        val topic = content.topicById(topicId)
        if (topic == null) {
            ErrorState("Тема не найдена")
            return@ContentGate
        }

        TopicDetailContent(
            details = TopicDetails(
                topic = topic,
                materials = content.materialsForTopic(topic.id)
            ),
            isSignedIn = uiState.session != null,
            isFavorite = topic.id in uiState.favoriteTopicIds,
            isBusy = uiState.isAuthLoading,
            onAuthClick = onAuthClick,
            onFavoriteClick = { onFavoriteClick(topic.id) }
        )
    }
}

@Composable
private fun TopicDetailContent(
    details: TopicDetails,
    isSignedIn: Boolean,
    isFavorite: Boolean,
    isBusy: Boolean,
    onAuthClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = details.topic.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = if (isSignedIn) onFavoriteClick else onAuthClick,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        !isSignedIn -> "Войти, чтобы добавить тему в избранное"
                        isFavorite -> "Убрать тему из избранного"
                        else -> "Добавить тему в избранное"
                    }
                )
            }
        }
        item { SectionTitle("Материалы по теме") }
        if (details.materials.isEmpty()) {
            item { EmptyInline("Материалы по этой теме пока не добавлены.") }
        } else {
            items(details.materials, key = { it.id }) { material ->
                MaterialCard(material)
            }
        }
    }
}

@Composable
private fun MaterialsScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit
) {
    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        if (content.materials.isEmpty()) {
            EmptyState("Дополнительные материалы пока не добавлены.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(content.materials, key = { it.id }) { material ->
                    MaterialCard(material)
                }
            }
        }
    }
}

@Composable
private fun EnhancedSearchScreen(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    onDisciplineClick: (Discipline) -> Unit,
    onTopicClick: (Topic) -> Unit
) {
    var queryField by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var areFiltersExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSpecialtyId by remember { mutableStateOf<String?>(null) }
    var selectedCourseNumber by remember { mutableStateOf<Int?>(null) }
    var selectedSemesterNumber by remember { mutableStateOf<Int?>(null) }

    val query = queryField.text
    val hasFilters = selectedSpecialtyId != null || selectedCourseNumber != null || selectedSemesterNumber != null

    ContentGate(uiState = uiState, onRetry = onRetry) { content ->
        val availableCourseNumbers = content.programData.availableCourseNumbers(selectedSpecialtyId)
        val availableSemesterNumbers = content.programData.availableSemesterNumbers(
            selectedSpecialtyId = selectedSpecialtyId,
            selectedCourseNumber = selectedCourseNumber
        )
        val searchData = if (query.isBlank() && !hasFilters) {
            SearchData()
        } else {
            content.search(
                query = query,
                selectedSpecialtyId = selectedSpecialtyId,
                selectedCourseNumber = selectedCourseNumber,
                selectedSemesterNumber = selectedSemesterNumber
            )
        }
        val activeFilters = content.programData.filterSummaryItems(
            selectedSpecialtyId = selectedSpecialtyId,
            selectedCourseNumber = selectedCourseNumber,
            selectedSemesterNumber = selectedSemesterNumber
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SearchSummaryCard(
                    query = query,
                    disciplinesCount = searchData.disciplines.size,
                    topicsCount = searchData.topics.size,
                    activeFilters = activeFilters,
                    onReset = {
                        queryField = TextFieldValue("")
                        selectedSpecialtyId = null
                        selectedCourseNumber = null
                        selectedSemesterNumber = null
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = queryField,
                    onValueChange = { queryField = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название дисциплины или темы") },
                    placeholder = { Text("Например, программирование") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    )
                )
            }

            item {
                CollapsibleProgramFiltersCard(
                    expanded = areFiltersExpanded,
                    activeFilters = activeFilters,
                    onExpandedChange = { areFiltersExpanded = it },
                    onReset = {
                        selectedSpecialtyId = null
                        selectedCourseNumber = null
                        selectedSemesterNumber = null
                    }
                ) {
                    ProgramFilters(
                        data = content.programData,
                        availableCourseNumbers = availableCourseNumbers,
                        availableSemesterNumbers = availableSemesterNumbers,
                        selectedSpecialtyId = selectedSpecialtyId,
                        selectedCourseNumber = selectedCourseNumber,
                        selectedSemesterNumber = selectedSemesterNumber,
                        onSpecialtySelected = {
                            selectedSpecialtyId = it
                            selectedCourseNumber = null
                            selectedSemesterNumber = null
                        },
                        onCourseSelected = {
                            selectedCourseNumber = it
                            selectedSemesterNumber = null
                        },
                        onSemesterSelected = {
                            selectedSemesterNumber = it
                        }
                    )
                }
            }

            searchResultsSection(
                query = query,
                hasFilters = hasFilters,
                data = searchData,
                onDisciplineClick = onDisciplineClick,
                onTopicClick = onTopicClick
            )
        }
    }
}

@Composable
private fun CollapsibleProgramFiltersCard(
    expanded: Boolean,
    activeFilters: List<Pair<String, String>>,
    onExpandedChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp).size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Фильтры учебной программы",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (activeFilters.isEmpty()) {
                            "Нажмите, чтобы выбрать специальность, курс и семестр только тогда, когда это нужно."
                        } else {
                            "Сейчас выбрано: " + activeFilters.joinToString(" • ") { "${it.first}: ${trimDisplayText(it.second, 18)}" }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть фильтры" else "Развернуть фильтры",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded && activeFilters.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeFilters) { (label, value) ->
                        SelectableFilterChip(
                            text = "$label: $value",
                            selected = true,
                            onClick = {}
                        )
                    }
                    item {
                        TextButton(onClick = onReset) {
                            Text("Сбросить")
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    content()
                }
            }
        }
    }
}

private fun LazyListScope.searchResultsSection(
    query: String,
    hasFilters: Boolean,
    data: SearchData,
    onDisciplineClick: (Discipline) -> Unit,
    onTopicClick: (Topic) -> Unit
) {
    if (query.isBlank() && !hasFilters) {
        item { EmptyInline("Введите запрос для поиска.") }
        return
    }

    if (data.disciplines.isEmpty() && data.topics.isEmpty()) {
        item {
            EmptyInline(
                if (query.isBlank()) {
                    "По выбранным фильтрам ничего не найдено."
                } else {
                    "Ничего не найдено. Измените запрос или снимите фильтры."
                }
            )
        }
        return
    }

    item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { CountBadge("Дисциплин", data.disciplines.size) }
            item { CountBadge("Тем", data.topics.size) }
        }
    }
    if (data.disciplines.isNotEmpty()) {
        item { SectionTitle("Дисциплины") }
        items(data.disciplines, key = { it.id }) { discipline ->
            DisciplineRow(discipline = discipline, onClick = { onDisciplineClick(discipline) })
        }
    }
    if (data.topics.isNotEmpty()) {
        item { SectionTitle("Темы") }
        items(data.topics, key = { it.id }) { topic ->
            TopicRow(topic = topic, onClick = { onTopicClick(topic) })
        }
    }
}

@Composable
private fun MaterialCard(material: Material) {
    val uriHandler = LocalUriHandler.current
    val normalizedType = remember(material.type, material.url) {
        inferMaterialType(material.type, material.url)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = material.title ?: "Материал без названия",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (normalizedType != null) {
                    if (!material.url.isNullOrBlank()) {
                        Button(
                            onClick = { uriHandler.openUri(material.url) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(MaterialTypeLabel(normalizedType))
                        }
                    } else {
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(MaterialTypeLabel(normalizedType)) }
                        )
                    }
                }
            }
            if (!material.description.isNullOrBlank()) {
                Text(text = material.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: EduMapUiState,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Настройки пользователя",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Здесь можно выбрать тему приложения, открыть личный кабинет и посмотреть информацию об EduMap.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SettingsCard(
                icon = Icons.Filled.Settings,
                title = "Оформление",
                description = "Выберите, как приложение будет выглядеть на этом телефоне."
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppThemeMode.values().toList()) { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.title) }
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(
                icon = Icons.Filled.Person,
                title = "Личный кабинет",
                description = if (uiState.session == null) {
                    "Войдите, чтобы сохранять избранные дисциплины, темы и заметки."
                } else {
                    "Вы вошли как ${uiState.session.login}. Аккаунт можно изменить в личном кабинете."
                }
            ) {
                OutlinedButton(
                    onClick = onProfileClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Открыть личный кабинет")
                }
            }
        }
        item {
            SettingsCard(
                icon = Icons.Filled.Info,
                title = "О приложении",
                description = "EduMap собирает учебную программу и полезные материалы в одном месте."
            ) {
                Text(
                    text = "Студент может без входа смотреть дисциплины, темы, теорию и источники. После авторизации появляются избранное и личные заметки.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.padding(10.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = trimDisplayText(text, 72),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun trimDisplayText(text: String, maxChars: Int): String {
    val normalized = text.trim().replace(Regex("\\s+"), " ")
    if (normalized.length <= maxChars) return normalized
    return normalized.take(maxChars).trimEnd() + "..."
}

@Composable
private fun ContentGate(
    uiState: EduMapUiState,
    onRetry: () -> Unit,
    content: @Composable (EduMapContent) -> Unit
) {
    when {
        uiState.content != null -> AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 16 })
        ) {
            content(uiState.content)
        }
        uiState.isLoading -> LoadingState()
        uiState.errorMessage != null -> ErrorState(
            message = uiState.errorMessage,
            onRetry = onRetry
        )
        else -> EmptyState("Данные пока не загружены.")
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Загрузка...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    val isNetworkError = "интернет" in message.lowercase() || "сеть" in message.lowercase()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isNetworkError) Icons.Filled.WifiOff else Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (isNetworkError) "Нет подключения" else "Ошибка загрузки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (onRetry != null) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyInline(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun MaterialTypeLabel(type: String): String =
    when (type.lowercase()) {
        "pdf" -> "PDF"
        "video" -> "Видео"
        "article" -> "Статья"
        "podcast" -> "Подкаст"
        "book" -> "Книга"
        "presentation" -> "Презентация"
        "document" -> "Документ"
        "image" -> "Изображение"
        "link" -> "Ссылка"
        else -> type
    }

private fun inferMaterialType(type: String?, url: String?): String? {
    val explicitType = type
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
    if (explicitType != null) {
        return when {
            explicitType.contains("video") -> "video"
            explicitType.contains("pdf") -> "pdf"
            explicitType.contains("presentation") -> "presentation"
            explicitType.contains("image") -> "image"
            explicitType.contains("book") -> "book"
            explicitType.contains("podcast") -> "podcast"
            explicitType.contains("article") -> "article"
            explicitType.contains("document") -> "document"
            explicitType.contains("link") -> "link"
            else -> explicitType
        }
    }

    val value = url?.trim()?.lowercase().orEmpty()
    if (value.isBlank()) return null
    return when {
        value.contains("youtube.com") || value.contains("youtu.be") || value.contains("rutube.ru") || value.contains("vkvideo.ru") -> "video"
        value.endsWith(".pdf") -> "pdf"
        Regex("""\.(ppt|pptx|odp)(\?|#|$)""").containsMatchIn(value) -> "presentation"
        Regex("""\.(doc|docx|odt|rtf|txt)(\?|#|$)""").containsMatchIn(value) -> "document"
        Regex("""\.(png|jpg|jpeg|webp|gif)(\?|#|$)""").containsMatchIn(value) -> "image"
        Regex("""\.(mp4|mov|avi|mkv|webm)(\?|#|$)""").containsMatchIn(value) -> "video"
        else -> "link"
    }
}

private fun ProgramData.filterSummaryItems(
    selectedSpecialtyId: String?,
    selectedCourseNumber: Int?,
    selectedSemesterNumber: Int?
): List<Pair<String, String>> = buildList {
    selectedSpecialtyId
        ?.let { id -> specialties.firstOrNull { it.id == id }?.name }
        ?.let { add("Специальность" to it) }
    selectedCourseNumber
        ?.let { add("Курс" to "$it курс") }
    selectedSemesterNumber
        ?.let { add("Семестр" to "$it семестр") }
}

@Composable
private fun CountBadgeRow(label: String, count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label —",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun ProgramData.availableCourseNumbers(selectedSpecialtyId: String?): List<Int> =
    courses
        .asSequence()
        .filter { it.number > 0 }
        .filter { selectedSpecialtyId == null || it.specialtyId == selectedSpecialtyId }
        .map { it.number }
        .distinct()
        .sorted()
        .toList()

private fun ProgramData.visibleCourseIds(
    selectedSpecialtyId: String?,
    selectedCourseNumber: Int?
): Set<String> =
    courses
        .asSequence()
        .filter { it.number > 0 }
        .filter { selectedSpecialtyId == null || it.specialtyId == selectedSpecialtyId }
        .filter { selectedCourseNumber == null || it.number == selectedCourseNumber }
        .map { it.id }
        .toSet()

private fun ProgramData.availableSemesterNumbers(
    selectedSpecialtyId: String?,
    selectedCourseNumber: Int?
): List<Int> {
    val visibleCourseIds = visibleCourseIds(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber
    )
    return semesters
        .asSequence()
        .filter { it.number > 0 }
        .filter { it.courseId in visibleCourseIds }
        .map { it.number }
        .distinct()
        .sorted()
        .toList()
}

private fun ProgramData.visibleSemesterIds(
    selectedSpecialtyId: String?,
    selectedCourseNumber: Int?,
    selectedSemesterNumber: Int?
): Set<String> {
    val visibleCourseIds = visibleCourseIds(
        selectedSpecialtyId = selectedSpecialtyId,
        selectedCourseNumber = selectedCourseNumber
    )
    return semesters
        .asSequence()
        .filter { it.number > 0 }
        .filter { it.courseId in visibleCourseIds }
        .filter { selectedSemesterNumber == null || it.number == selectedSemesterNumber }
        .map { it.id }
        .toSet()
}

@Composable
private fun CountBadge(label: String, count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private data class DisciplineDetails(
    val discipline: Discipline,
    val topics: List<Topic>,
    val materials: List<Material>,
    val courseNumber: Int? = null,
    val semesterNumber: Int? = null,
    val specialtyName: String? = null
)

private data class TopicDetails(
    val topic: Topic,
    val materials: List<Material>
)
