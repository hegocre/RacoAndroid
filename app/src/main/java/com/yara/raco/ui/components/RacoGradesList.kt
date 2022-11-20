package com.yara.raco.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.yara.raco.R
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationController
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoEvaluationTabs(
    subjects: List<Subject>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        Spacer(
            Modifier
                .pagerTabIndicatorOffset(pagerState, tabPositions)
                .padding(horizontal = 12.dp)
                .height(3.dp)
                .background(
                    LocalContentColor.current,
                    RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)
                )
        )
    }
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = modifier,
        indicator = indicator,
        edgePadding = 32.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        Tab(
            selected = pagerState.currentPage == 0,
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0)
                }
            },
            text = { Text(text = stringResource(id = R.string.all)) }
        )
        subjects.forEachIndexed() { index, subject ->
            Tab(
                selected = pagerState.currentPage == index + 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index + 1)
                    }
                },
                text = { Text(text = subject.sigles) }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoGradesPager(
    pagerState: PagerState,
    subjects: List<Subject>,
    evaluations: List<EvaluationWithGrade>,
    onGradeAddOrUpdate: (grade: Grade, evaluation: Evaluation) -> Unit,
    onGradeDelete: (grade: Grade, evaluation: Evaluation) -> Unit,
    onEvaluationAdd: (subjectId: String) -> Unit,
    onEvaluationDelete: (evaluation: Evaluation) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        count = subjects.size + 1,
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        LazyColumn() {
            when (page) {
                0 -> {
                    items(evaluations) { evaluation -> RacoGradesCollapsed(evaluation = evaluation) }
                    item {
                        OutlinedCard(
                            border = CardDefaults.outlinedCardBorder(enabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            )
                            {
                                Text(
                                    text = "Add new evaluation",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                IconButton(onClick = { onEvaluationAdd("-1") }) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Add Evaluation")
                                }
                            }
                        }
                    }
                }
                else -> {
                    items(evaluations.filter { it.evaluation.subjectId == subjects[page - 1].id }) { evaluationSubject ->
                        RacoGradesExpanded(
                            evaluation = evaluationSubject,
                            onEvaluationAdd = onEvaluationAdd
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun RacoGradesCollapsed(
    evaluation: EvaluationWithGrade,
) {
    OutlinedCard(
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Text(
                text = evaluation.evaluation.subjectId,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = evaluation.evaluation.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "%.${2}f".format(computeFinalMarkFromEvaluation(evaluation.listOfGrade)),
                style = MaterialTheme.typography.titleLarge,
                color = when (computeFinalMarkFromEvaluation(evaluation.listOfGrade)) {
                    in 0.0..4.9 -> MaterialTheme.colorScheme.error
                    in 5.0..6.9 -> MaterialTheme.colorScheme.secondary
                    in 7.0..10.0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun RacoGradesExpanded(
    evaluation: EvaluationWithGrade,
    onEvaluationAdd: (subjectId: String) -> Unit

) {
    var editWeight by rememberSaveable { mutableStateOf(false) }
    var neededMark by rememberSaveable { mutableStateOf(computeAverageMarkForPassing(evaluation.listOfGrade)) }
    var subjectId = evaluation.evaluation.subjectId
    OutlinedCard(
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    text = "${evaluation.evaluation.name}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "%.${2}f".format(computeFinalMarkFromEvaluation(evaluation.listOfGrade)),
                    style = MaterialTheme.typography.titleLarge,
                    color = when (computeFinalMarkFromEvaluation(evaluation.listOfGrade)) {
                        in 0.0..4.9 -> MaterialTheme.colorScheme.error
                        in 5.0..6.9 -> MaterialTheme.colorScheme.secondary
                        in 7.0..10.0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                IconButton(onClick = { editWeight = !editWeight }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit Weight")
                }
            }
            Crossfade(
                targetState = editWeight
            ) { editWeight ->
                if (!editWeight) {
                    Column() {
                        for (grade in evaluation.listOfGrade)
                            RacoGradeMark(
                                grade = grade,
                                neededMark = neededMark,
                            )
                    }

                } else {
                    Column() {
                        for (grade in evaluation.listOfGrade)
                            RacoGradeWeight(grade = grade)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    text = "Add new weight",
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { editWeight = !editWeight }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Weight")
                }
            }
        }
    }
    OutlinedCard(
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Text(
                text = "Add new evaluation",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = { onEvaluationAdd(subjectId) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Evaluation")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeMark(
    grade: Grade,
    neededMark: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (grade.description != "") "${grade.name}\n${grade.description}" else "${grade.name}",
            style = MaterialTheme.typography.titleSmall,
        )
        var gradeMark by rememberSaveable { mutableStateOf(grade.mark.toString()) }
        if (gradeMark.toDouble() < 0.0) gradeMark = ""
        OutlinedTextField(
            value = gradeMark,
            placeholder = {
                Text(
                    text = neededMark.toString(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            onValueChange = { gradeMark = it },
            modifier = Modifier.width(150.dp),
            maxLines = 1,
            leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = "Grade Mark") }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeWeight(
    grade: Grade,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = grade.name,
            style = MaterialTheme.typography.titleSmall,
        )
        var gradeWeight by rememberSaveable { mutableStateOf(grade.weight.toString()) }
        OutlinedTextField(
            value = gradeWeight,
            onValueChange = { gradeWeight = it },
            modifier = Modifier.width(150.dp),
            maxLines = 1,
            leadingIcon = { Icon(Icons.Outlined.Percent, contentDescription = "Grade Mark") }
        )
    }
}

fun computeFinalMarkFromEvaluation(
    evaluation: List<Grade>
): Double {
    var mark = 0.0
    for (grade in evaluation) {
        if (0.0 <= grade.mark)
            mark += grade.mark * (grade.weight / 100)
    }
    return mark
}

fun computeAverageMarkForPassing(
    evaluation: List<Grade>
): Double {
    var accumulatedWeight = 0.0
    var cursedWeight = 0.0
    for (grade in evaluation) {
        accumulatedWeight += grade.weight
        if (0.0 <= grade.mark)
            cursedWeight += grade.weight
    }
    var currentMark = computeFinalMarkFromEvaluation(evaluation)

    return (5.0 * accumulatedWeight - currentMark * cursedWeight) / (accumulatedWeight - cursedWeight)
}