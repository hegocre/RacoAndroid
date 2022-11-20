package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.grade.Grade
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

    val context = LocalContext.current

    RacoTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                RacoMainTopAppBar(
                    title = stringResource(id = currentScreen.title),
                    scrollBehavior = scrollBehavior,
                    onLogOut = onLogOut,
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
            val evaluations by racoViewModel.evaluation.observeAsState(initial = emptyList())
            RacoMainNavHost(
                navHostController = navController,
                noticesWithFiles = sortedNoticesWithFiles,
                evaluationWithGrade = evaluations,
                onFileClick = { file -> racoViewModel.downloadFile(file) },
                onGradeAddOrUpdate = { grade: Grade, evaluation: Evaluation ->
                    racoViewModel.addOrUpdateGradeToEvaluation(
                        grade,
                        evaluation
                    )
                },
                onGradeDelete = { grade: Grade, evaluation: Evaluation ->
                    racoViewModel.removeGradeFromEvaluation(
                        grade,
                        evaluation
                    )
                },
                onEvaluationAdd = { subjectId: String -> racoViewModel.addEvaluation(subjectId) },
                onEvaluationDelete = { evaluation: Evaluation ->
                    racoViewModel.deleteEvaluation(
                        evaluation
                    )
                },
                subjects = sortedSubjects,
                modifier = Modifier.padding(paddingValues),
                onRefresh = { racoViewModel.refresh() },
                isRefreshing = isRefreshing
            )
        }
    }
}