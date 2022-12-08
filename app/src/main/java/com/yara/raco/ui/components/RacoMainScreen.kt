package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.activities.AboutActivity
import com.yara.raco.ui.theme.RacoTheme
import com.yara.raco.ui.viewmodel.RacoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoMainScreen(
    racoViewModel: RacoViewModel,
    onLogOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = RacoScreen.fromRoute(
        backStackEntry.value?.destination?.route
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

    val context = LocalContext.current

    var dayCalendarViewSelected by rememberSaveable{ mutableStateOf(true)}

    val onBackPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        //Declare back action for button to appear
        "${RacoScreen.Avisos.name}/details" -> {
            {
                navController.popBackStack()
            }
        }
        //Default to not visible
        else -> null
    }

    val onEventSettingsPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        "${RacoScreen.Horari.name}" -> {
            {
                dayCalendarViewSelected = !dayCalendarViewSelected
            }
        }
        //Default to not visible
        else -> null
    }

    RacoTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                RacoMainTopAppBar(
                    title = stringResource(id = currentScreen.title),
                    scrollBehavior = scrollBehavior,
                    onLogOut = onLogOut,
                    onBackPress = onBackPress,
                    onEventSettingsPress = onEventSettingsPress,
                    onAbout = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    }
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
            contentWindowInsets = WindowInsets.systemBars,
        ) { paddingValues ->
            val noticesWithFiles by racoViewModel.notices.observeAsState(initial = emptyList())
            val sortedNoticesWithFiles = remember(noticesWithFiles) {
                noticesWithFiles.sortedByDescending { it.notice.dataModificacio }
            }
            val subjects by racoViewModel.subjects.observeAsState(initial = emptyList())
            val sortedSubjects = remember(subjects) {
                subjects.sortedBy { it.nom }
            }
            val schedules by racoViewModel.schedules.observeAsState(initial = emptyList())
            RacoMainNavHost(
                navHostController = navController,
                noticesWithFiles = sortedNoticesWithFiles,
                onFileClick = { file -> racoViewModel.downloadFile(file) },
                subjects = sortedSubjects,
                schedules = schedules,
                dayCalendarViewSelected = dayCalendarViewSelected,
                modifier = Modifier.padding(paddingValues),
                onRefresh = { racoViewModel.refresh() },
                isRefreshing = isRefreshing
            )
        }
    }
}