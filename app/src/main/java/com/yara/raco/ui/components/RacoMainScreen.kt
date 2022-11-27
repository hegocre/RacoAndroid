package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.NoticeWithFiles
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
    var editDetailedEvaluation by remember { mutableStateOf<Boolean>(false) }
    val evaluations by racoViewModel.evaluation.observeAsState(initial = emptyList())

    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = RacoScreen.fromRoute(
        backStackEntry.value?.destination?.route
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

    val context = LocalContext.current

    val onBackPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        //Declare back action for button to appear
        "${RacoScreen.Avisos.name}/details" -> {
            {
                navController.popBackStack()
            }
        }
        "${RacoScreen.Notes.name}/details" -> {
            {
                editDetailedEvaluation = false
                navController.popBackStack()
            }
        }
        //Default to not visible
        else -> null
    }

    val onEditPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        "${RacoScreen.Notes.name}/details" -> {
            {
                editDetailedEvaluation = !editDetailedEvaluation
            }
        }
        //Default to not visible
        else -> null
    }

    val onAddPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        "${RacoScreen.Notes.name}" -> {
            {
                racoViewModel.addEvaluation("")
            }
        }
        //Default to not visible
        else -> null
    }

    val onDeleteEvaluation: ((Int) -> Unit) = {
        navController.popBackStack()
        racoViewModel.deleteEvaluation(it)
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
                    onEditPress = onEditPress,
                    onAddPress = onAddPress,
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
            RacoMainNavHost(
                navHostController = navController,
                noticesWithFiles = sortedNoticesWithFiles,
                evaluationWithGrade = evaluations,
                onFileClick = { file -> racoViewModel.downloadFile(file) },
                onGradeAdd = { grade -> racoViewModel.addGradeToEvaluation(grade) },
                onGradeDelete = { grade -> racoViewModel.deleteGrade(grade) },
                onEvaluationDelete = onDeleteEvaluation,
                onGradeDetailedEdit = editDetailedEvaluation,
                subjects = sortedSubjects,
                modifier = Modifier.padding(paddingValues),
                onRefresh = { racoViewModel.refresh() },
                isRefreshing = isRefreshing
            )
        }
    }
}
