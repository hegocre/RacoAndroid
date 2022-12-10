package com.yara.raco.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.schedule.Schedule
import com.yara.raco.model.subject.Subject
import com.yara.raco.ui.RacoScreen

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoMainNavHost(
    navHostController: NavHostController,
    noticesWithFiles: List<NoticeWithFiles>,
    evaluationWithGrade: List<EvaluationWithGrade>,
    onFileClick: (File) -> Unit,
    onEvaluationUpdate: (EvaluationWithGrade) -> Unit,
    onEvaluationDelete: (Int) -> Unit,
    onAddEvaluationClick: () -> Unit,
    subjects: List<Subject>,
    schedules: List<Schedule>,
    dayCalendarViewSelected: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var detailedNoticeWithFiles by rememberSaveable(saver = NoticeWithFiles.Saver) {
        mutableStateOf(
            null
        )
    }
    var detailedEvaluationWithGrades by rememberSaveable(saver = EvaluationWithGrade.Saver) {
        mutableStateOf(
            null
        )
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
                    subjects = subjects,
                    pagerState = pagerState,
                )

                RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    RacoNoticePager(
                        pagerState = pagerState,
                        subjects = subjects,
                        noticesWithFiles = noticesWithFiles,
                        onNoticeClick = { noticeWithFiles ->
                            detailedNoticeWithFiles = noticeWithFiles
                            navHostController.navigate("${RacoScreen.Avisos.name}/details")
                        }
                    )
                }
            }
        }

        composable("${RacoScreen.Avisos.name}/details") {
            Column {
                detailedNoticeWithFiles?.let { noticeWithFiles ->
                    DetailedNoticeWithFiles(
                        noticeWithFiles = noticeWithFiles,
                        onFileClick = onFileClick
                    )
                }
            }
        }

        composable(RacoScreen.Horari.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
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
            Column {
                RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    RacoGradesList(
                        subjects = subjects,
                        evaluations = evaluationWithGrade,
                        onGradeClick = { evaluationWithGrade ->
                            detailedEvaluationWithGrades = evaluationWithGrade
                            navHostController.navigate("${RacoScreen.Notes.name}/details")
                        },
                        onAddEvaluationClick = onAddEvaluationClick
                    )
                }
            }
        }

        composable("${RacoScreen.Notes.name}/details") {
            Column {
                detailedEvaluationWithGrades?.let { evaluationWithGrades ->
                    DetailedEvaluationWithGradeCall(
                        subjects = subjects,
                        evaluation = evaluationWithGrades,
                        onEvaluationUpdate = onEvaluationUpdate,
                        onEvaluationDelete = onEvaluationDelete
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