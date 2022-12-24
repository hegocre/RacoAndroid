package com.yara.raco.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.viewmodel.RacoViewModel
import com.yara.raco.utils.Result

data class DetailsUiState<T>(
    val detailed: T? = null,
    val isLoading: Boolean = false,
    val throwError: Boolean = false
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoMainNavHost(
    navHostController: NavHostController,
    racoViewModel: RacoViewModel,
    dayCalendarViewSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

    var detailedEvaluationWithGrades by rememberSaveable(saver = EvaluationWithGrade.Saver) {
        mutableStateOf(
            null
        )
    }

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

    NavHost(
        navController = navHostController,
        startDestination = RacoScreen.Avisos.name,
        modifier = modifier
    ) {
        composable(RacoScreen.Avisos.name) {
            Column {
                val pagerState = rememberPagerState()

                RacoNoticeTabs(
                    subjects = sortedSubjects,
                    pagerState = pagerState,
                )

                RacoSwipeRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = { racoViewModel.refresh() }) {
                    RacoNoticePager(
                        pagerState = pagerState,
                        subjects = sortedSubjects,
                        noticesWithFiles = sortedNoticesWithFiles,
                        onNoticeClick = { noticeWithFiles ->
                            navHostController.navigate(
                                "${RacoScreen.Avisos.name}/details/${noticeWithFiles.notice.id}"
                            )
                        }
                    )
                }
            }
        }

        composable(
            route = "${RacoScreen.Avisos.name}/details/{notice_id}",
            arguments = listOf(
                navArgument("notice_id") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            entry.arguments?.getInt("notice_id")?.let { noticeId ->
                val detailedNoticeState by produceState(
                    initialValue = DetailsUiState<NoticeWithFiles>(
                        isLoading = true
                    )
                ) {
                    val noticeWithFilesResult = racoViewModel.getNoticeDetails(noticeId)
                    value = if (noticeWithFilesResult is Result.Success<NoticeWithFiles>) {
                        DetailsUiState(noticeWithFilesResult.data)
                    } else {
                        DetailsUiState(throwError = true)
                    }
                }

                when {
                    detailedNoticeState.detailed != null -> {
                        detailedNoticeState.detailed?.let { noticeWithFiles ->
                            Column {
                                DetailedNoticeWithFiles(
                                    noticeWithFiles = noticeWithFiles,
                                    onFileClick = { file -> racoViewModel.downloadFile(file) }
                                )
                            }
                        }
                    }
                    detailedNoticeState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                    detailedNoticeState.throwError -> {
                        Text(text = "error")
                    }
                }
            }
        }

        composable(RacoScreen.Horari.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = { racoViewModel.refresh() }) {
                Crossfade(targetState = dayCalendarViewSelected) { isDayCalendarViewSelected ->
                    if (isDayCalendarViewSelected) {
                        RacoScheduleDay(schedules = schedules)
                    } else {
                        RacoScheduleWeek(schedules = schedules)
                    }
                }
            }
        }

        composable(RacoScreen.Notes.name) {
            var showAddEvaluationDialog by remember { mutableStateOf(false) }

            Column {
                RacoSwipeRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = { racoViewModel.refresh() }) {
                    RacoGradesList(
                        subjects = sortedSubjects,
                        evaluations = sortedEvaluations,
                        onGradeClick = { evaluationWithGrade ->
                            detailedEvaluationWithGrades = evaluationWithGrade
                            navHostController.navigate("${RacoScreen.Notes.name}/details")
                        },
                        onAddEvaluationClick = { showAddEvaluationDialog = true }
                    )
                }
            }

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

        composable("${RacoScreen.Notes.name}/details") {
            Column {
                detailedEvaluationWithGrades?.let { evaluationWithGrades ->
                    DetailedEvaluationWithGradeCall(
                        subjects = sortedSubjects,
                        evaluation = evaluationWithGrades,
                        onEvaluationUpdate = { evaluationWithGrade ->
                            racoViewModel.evaluationSave(
                                evaluationWithGrade
                            )
                        },
                        onEvaluationDelete = {
                            navHostController.popBackStack()
                            racoViewModel.deleteEvaluation(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RacoSwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val refreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = refreshState,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicator = { rState, refreshTrigger ->
            SwipeRefreshIndicator(
                state = rState,
                refreshTriggerDistance = refreshTrigger,
                contentColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            )
        }
    ) {
        content()
    }
}