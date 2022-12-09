package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.activities.AboutActivity
import com.yara.raco.ui.theme.RacoTheme
import com.yara.raco.ui.viewmodel.RacoViewModel
import java.util.*

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

    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

    val context = LocalContext.current

    var dayCalendarViewSelected by rememberSaveable{ mutableStateOf(true)}

    val onBackPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        //Declare back action for button to appear
        "${RacoScreen.Avisos.name}/details", "${RacoScreen.Notes.name}/details" -> {
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

    val onDeleteEvaluation: ((Int) -> Unit) = {
        navController.popBackStack()
        racoViewModel.deleteEvaluation(it)
    }

    var showAddEvaluationDialog by remember { mutableStateOf(false) }

    RacoTheme {
        Scaffold(
            topBar = {
                RacoMainTopAppBar(
                    title = stringResource(id = currentScreen.title),
                    onLogOut = onLogOut,
                    onBackPress = onBackPress,
                    isDayViewSelected = dayCalendarViewSelected,
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
            val evaluations by racoViewModel.evaluation.observeAsState(initial = emptyList())
            val sortedEvaluations = remember(evaluations) {
                evaluations.sortedBy { it.evaluation.name }
            }

            RacoMainNavHost(
                navHostController = navController,
                noticesWithFiles = sortedNoticesWithFiles,
                evaluationWithGrade = sortedEvaluations,
                onFileClick = { file -> racoViewModel.downloadFile(file) },
                onEvaluationUpdate = { evaluationWithGrade ->
                    racoViewModel.evaluationSave(
                        evaluationWithGrade
                    )
                },
                onEvaluationDelete = onDeleteEvaluation,
                onAddEvaluationClick = { showAddEvaluationDialog = true },
                subjects = sortedSubjects,
                schedules = schedules,
                dayCalendarViewSelected = dayCalendarViewSelected,
                modifier = Modifier.padding(paddingValues),
                onRefresh = { racoViewModel.refresh() },
                isRefreshing = isRefreshing
            )

            if (showAddEvaluationDialog) {
                AddEvaluationDialog(
                    subjects = sortedSubjects,
                    onAddClick = { subjectId, evaluationName ->
                        racoViewModel.addEvaluation(subjectId, evaluationName)
                        showAddEvaluationDialog = false
                    },
                    onDismissRequest = { showAddEvaluationDialog = false }
                )
            }
        }
    }
}