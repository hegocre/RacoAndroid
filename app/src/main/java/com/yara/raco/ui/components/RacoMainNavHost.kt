package com.yara.raco.ui.components

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yara.raco.R
import com.yara.raco.model.evaluation.EvaluationWithGrades
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.viewmodel.RacoViewModel
import com.yara.raco.utils.Result
import java.util.*

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
    dayPagerState: PagerState,
    weekPagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val isRefreshing by racoViewModel.isRefreshing.collectAsState()

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

    val exams by racoViewModel.exams.observeAsState(initial = emptyList())

    NavHost(
        navController = navHostController,
        startDestination = RacoScreen.Notes.name,
        modifier = modifier
    ) {
        composable(RacoScreen.Notes.name) {
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
                                "${RacoScreen.Notes.name}/details/${noticeWithFiles.notice.id}"
                            )
                        }
                    )
                }
            }
        }

        composable(
            route = "${RacoScreen.Notes.name}/details/{notice_id}",
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
                        Text(
                            text = stringResource(id = R.string.error_message),
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        composable(RacoScreen.Schedule.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = { racoViewModel.refresh() }) {
                Crossfade(targetState = dayCalendarViewSelected) { isDayCalendarViewSelected ->
                    if (isDayCalendarViewSelected) {
                        RacoScheduleDay(
                            schedules = schedules,
                            exams = exams,
                            setTitle = racoViewModel::setCalendarShowingTitle,
                            pagerState = dayPagerState,
                            onEventClick = { event -> racoViewModel.setCalendarDialogEvent(event) }
                        )
                    } else {
                        RacoScheduleWeek(
                            schedules = schedules,
                            exams = exams,
                            setTitle = racoViewModel::setCalendarShowingTitle,
                            pagerState = weekPagerState,
                            onEventClick = { event -> racoViewModel.setCalendarDialogEvent(event) }
                        )
                    }
                }

                racoViewModel.calendarDialogEvent?.let { event ->
                    ScheduleEventDetailsDialog(
                        scheduleEvent = event,
                        onDismissRequest = { racoViewModel.setCalendarDialogEvent(null) }
                    )
                }
            }
        }

        composable(RacoScreen.Grades.name) {
            var showAddEvaluationDialog by remember { mutableStateOf(false) }

            Column {
                RacoEvaluationList(
                    subjects = sortedSubjects,
                    evaluations = sortedEvaluations,
                    onGradeClick = { evaluationWithGrade ->
                        navHostController.navigate("${RacoScreen.Grades.name}/details/${evaluationWithGrade.evaluation.id}")
                    },
                    onAddEvaluationClick = { showAddEvaluationDialog = true }
                )
            }

            if (showAddEvaluationDialog) {
                if (sortedSubjects.isNotEmpty()) {
                    AddEvaluationDialog(
                        subjects = sortedSubjects,
                        onAddClick = { subjectId, evaluationName ->
                            racoViewModel.addEvaluation(subjectId, evaluationName)
                            showAddEvaluationDialog = false
                        },
                        onDismissRequest = { showAddEvaluationDialog = false }
                    )
                } else {
                    Toast.makeText(
                        LocalContext.current,
                        stringResource(id = R.string.no_subjects),
                        Toast.LENGTH_LONG
                    ).show()
                    showAddEvaluationDialog = false
                }
            }
        }

        composable(
            route = "${RacoScreen.Grades.name}/details/{evaluation_id}",
            arguments = listOf(
                navArgument("evaluation_id") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            entry.arguments?.getInt("evaluation_id")?.let { evaluationId ->
                val detailedEvaluation by racoViewModel.getLiveEvaluationDetails(evaluationId)
                    .observeAsState()

                detailedEvaluation?.let { evaluationWithGrades ->
                    Column {
                        DetailedEvaluation(
                            evaluation = evaluationWithGrades,
                            onEditClick = {
                                navHostController.navigate("${RacoScreen.Grades.name}/details/${evaluationId}/edit")
                            },
                            onGradeUpdate = { updatedGrade -> racoViewModel.updateGrade(updatedGrade) },
                        )
                    }
                }
            }
        }

        composable(
            route = "${RacoScreen.Grades.name}/details/{evaluation_id}/edit",
            arguments = listOf(
                navArgument("evaluation_id") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            entry.arguments?.getInt("evaluation_id")?.let { evaluationId ->
                val detailedEvaluationState by produceState(
                    initialValue = DetailsUiState<EvaluationWithGrades>(
                        isLoading = true
                    )
                ) {
                    val noticeWithFilesResult = racoViewModel.getEvaluationDetails(evaluationId)
                    value = if (noticeWithFilesResult is Result.Success<EvaluationWithGrades>) {
                        DetailsUiState(noticeWithFilesResult.data)
                    } else {
                        DetailsUiState(throwError = true)
                    }
                }

                when {
                    detailedEvaluationState.detailed != null -> {
                        detailedEvaluationState.detailed?.let { detailedEvaluation ->
                            val editableEvaluationState =
                                rememberEditableEvaluationState(evaluationWithGrades = detailedEvaluation)
                            val context = LocalContext.current

                            Column {
                                EditableEvaluation(
                                    editableEvaluationState = editableEvaluationState,
                                    onEvaluationSave = {
                                        try {
                                            racoViewModel.saveEvaluation(editableEvaluationState.getEvaluationWithGrades())
                                            navHostController.popBackStack()
                                        } catch (e: IllegalArgumentException) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.evaluation_invalid_arguments),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    onEvaluationDelete = {
                                        racoViewModel.deleteEvaluation(evaluationId)
                                        navHostController.popBackStack(
                                            RacoScreen.Grades.name, inclusive = false
                                        )
                                    },
                                )
                            }
                        }
                    }
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