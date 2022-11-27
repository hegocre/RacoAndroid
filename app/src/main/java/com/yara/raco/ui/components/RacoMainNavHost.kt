package com.yara.raco.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject
import com.yara.raco.ui.RacoScreen

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoMainNavHost(
    navHostController: NavHostController,
    noticesWithFiles: List<NoticeWithFiles>,
    evaluationWithGrade: List<EvaluationWithGrade>,
    onFileClick: (File) -> Unit,
    onGradeAdd: (Int) -> Unit,
    onGradeDelete: (Int) -> Unit,
    onEvaluationDelete: (Int) -> Unit,
    onGradeDetailedEdit: Boolean,
    subjects: List<Subject>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var detailedNoticeWithFiles by remember { mutableStateOf<NoticeWithFiles?>(null) }
    var detailedGrade by remember { mutableStateOf<Int?>(null) }
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
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                }
            }
        }

        composable(RacoScreen.Notes.name) {
            Column {
                RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    RacoGradesPager(
                        subjects = subjects,
                        evaluations = evaluationWithGrade,
                        onGradeClick = { evaluationWithGrade ->
                            detailedGrade = evaluationWithGrade.evaluation.id
                            navHostController.navigate("${RacoScreen.Notes.name}/details")
                        }
                    )
                }
            }
        }

        composable("${RacoScreen.Notes.name}/details") {
            Column {
                detailedGrade?.let { evaluationId ->
                    DetailedEvaluationWithGradeCall(
                        evaluation = evaluationWithGrade.first { it.evaluation.id == evaluationId },
                        onGradeDetailedEdit = onGradeDetailedEdit,
                        onGradeAdd = onGradeAdd,
                        onGradeDelete = onGradeDelete,
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