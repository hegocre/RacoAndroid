package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.yara.raco.R
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.activities.AboutActivity
import com.yara.raco.ui.theme.RacoTheme
import com.yara.raco.ui.viewmodel.RacoViewModel
import com.yara.raco.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalPagerApi::class)
@Composable
fun RacoMainScreen(
    racoViewModel: RacoViewModel,
    notificationNoticeId: Int,
    onLogOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = RacoScreen.fromRoute(
        backStackEntry.value?.destination?.route
    )

    val context = LocalContext.current
    val preferencesManager = PreferencesManager.getInstance(context)

    val showAllNoticesSelected by preferencesManager.getShowAllNoticesSelected().collectAsState(
        initial = true
    )
    val dayCalendarViewSelected by preferencesManager.getDayCalendarViewSelected().collectAsState(
        initial = false
    )

    val currentRoute = backStackEntry.value?.destination?.route
    val onBackPress: (() -> Unit)? = when {
        //Declare back action for button to appear
        currentRoute?.startsWith("${RacoScreen.Grades.name}/details") == true ||
                currentRoute?.startsWith("${RacoScreen.Notes.name}/details") == true -> {
            {
                navController.popBackStack()
            }
        }
        //Default to not visible
        else -> null
    }

    var showLogOutDialog by rememberSaveable { mutableStateOf(false) }

    val today = remember {
        LocalDateTime.now()
    }
    val firstCalendarDay = remember {
        today.minusMonths(4L).with(
            TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        )
    }
    val dayPagerState = rememberPagerState(
        initialPage = ChronoUnit.DAYS.between(firstCalendarDay, today).toInt()
    )
    val weekPagerState = rememberPagerState(
        initialPage = ChronoUnit.WEEKS.between(firstCalendarDay, today).toInt()
    )

    val dropdownActions = mapOf(
        stringResource(id = R.string.about) to {
            context.startActivity(Intent(context, AboutActivity::class.java))
        },
        stringResource(id = R.string.logout) to { showLogOutDialog = true }
    )

    val coroutineScope = rememberCoroutineScope()
    val iconActions: Map<Pair<ImageVector, String>, () -> Unit>? =
        when (backStackEntry.value?.destination?.route) {
            RacoScreen.Schedule.name -> mapOf(
                Pair(
                    Icons.Default.Today, stringResource(id = R.string.scroll_to_today)
                ) to {
                    if (dayCalendarViewSelected) {
                        coroutineScope.launch {
                            dayPagerState.animateScrollToPage(
                                ChronoUnit.DAYS.between(
                                    firstCalendarDay,
                                    today
                                ).toInt()
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            weekPagerState.animateScrollToPage(
                                ChronoUnit.WEEKS.between(
                                    firstCalendarDay,
                                    today
                                ).toInt()
                            )
                        }
                    }
                },
                Pair(
                    if (dayCalendarViewSelected) Icons.Default.ViewWeek else Icons.Default.ViewDay,
                    if (dayCalendarViewSelected) stringResource(id = R.string.week) else stringResource(
                        id = R.string.day
                    )
                ) to {
                    coroutineScope.launch {
                        preferencesManager.setDayCalendarViewSelected(!dayCalendarViewSelected)
                    }
                }
            )
            RacoScreen.Notes.name -> mapOf(
                Pair(
                    if (showAllNoticesSelected) Icons.Default.MarkEmailUnread else Icons.Default.Drafts,
                    if (showAllNoticesSelected) stringResource(id = R.string.show_unread_notices) else stringResource(
                        id = R.string.show_all_notices
                    )
                ) to {
                    coroutineScope.launch {
                        preferencesManager.setShowAllNoticesSelected(!showAllNoticesSelected)
                    }
                }
            )
            else -> null
        }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )


    RacoTheme {
        Scaffold(
            topBar = {
                RacoMainTopAppBar(
                    title = when (currentScreen) {
                        RacoScreen.Schedule -> racoViewModel.calendarShowingTitle
                        else -> stringResource(id = currentScreen.title)
                    },
                    scrollBehavior = scrollBehavior,
                    onBackPress = onBackPress,
                    iconActions = iconActions,
                    dropdownActions = dropdownActions
                )
            },
            bottomBar = {
                RacoMainNavigationBar(
                    allScreens = RacoScreen.values().toList(),
                    currentScreen = currentScreen,
                    onScreenSelected = { racoScreen ->
                        navController.navigate(racoScreen.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
        ) { paddingValues ->
            RacoMainNavHost(
                navHostController = navController,
                racoViewModel = racoViewModel,
                showAllNoticesSelected = showAllNoticesSelected,
                dayCalendarViewSelected = dayCalendarViewSelected,
                dayPagerState = dayPagerState,
                weekPagerState = weekPagerState,
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues)
                    .consumedWindowInsets(paddingValues),
            )

            if (showLogOutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogOutDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogOutDialog = false
                            onLogOut()
                        }) {
                            Text(text = stringResource(id = R.string.logout))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogOutDialog = false }) {
                            Text(text = stringResource(id = android.R.string.cancel))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.logout)) },
                    text = { Text(text = stringResource(id = R.string.logout_confirmation)) }
                )
            }
        }
    }

    LaunchedEffect(key1 = notificationNoticeId) {
        if (notificationNoticeId != -1) {
            navController.navigate("${RacoScreen.Notes.name}/details/${notificationNoticeId}")
        }
    }
}