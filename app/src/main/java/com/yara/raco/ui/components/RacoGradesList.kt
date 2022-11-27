package com.yara.raco.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.yara.raco.R
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.subject.Subject
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun RacoGradesPager(
    subjects: List<Subject>,
    evaluations: List<EvaluationWithGrade>,
    onGradeClick: (EvaluationWithGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectsIds = (evaluations.map { it.evaluation.subjectId }).distinct()
    LazyColumn() {
        for (subject in subjectsIds) {
            val subjectName = subjects.find { it.sigles == subject }?.nom
            stickyHeader {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (subject != "") subjectName.toString() else "Unlabeled",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            items(evaluations.filter { it.evaluation.subjectId == subject }) { evaluation ->
                RacoGradesCollapsed(
                    evaluation = evaluation,
                    onGradeClick = onGradeClick
                )
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillParentMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

@Composable
fun DetailedEvaluationWithGradeCall(
    evaluation: EvaluationWithGrade,
    onGradeDetailedEdit: Boolean,
    onGradeAdd: (Int) -> Unit,
    onGradeDelete: (Int) -> Unit,
    onEvaluationDelete: (Int) -> Unit,
) {
    if (!onGradeDetailedEdit) {
        DetailedEvaluationWithGrade(evaluation = evaluation)
    } else {
        EditEvaluationWithGrade(
            evaluation = evaluation,
            onGradeAdd = onGradeAdd,
            onGradeDelete = onGradeDelete,
            onEvaluationDelete = onEvaluationDelete
        )
    }
}

@Composable
fun DetailedEvaluationWithGrade(
    evaluation: EvaluationWithGrade,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = evaluation.evaluation.name,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = evaluation.evaluation.subjectId,
            style = MaterialTheme.typography.headlineLarge
        )
        gradeMarkWithColor(
            computeFinalMarkFromEvaluation(evaluation.listOfGrade),
            MaterialTheme.typography.headlineMedium
        )
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (grade in evaluation.listOfGrade) {
            RacoGradeView(
                grade = grade,
                neededMark = 0.0,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationWithGrade(
    evaluation: EvaluationWithGrade,
    onGradeAdd: (Int) -> Unit,
    onGradeDelete: (Int) -> Unit,
    onEvaluationDelete: (Int) -> Unit,
) {
    var evaluationName by remember { mutableStateOf<String>(evaluation.evaluation.name) }
    var evaluationSubjectId by remember { mutableStateOf<String>(evaluation.evaluation.subjectId) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = evaluationName,
                placeholder = {
                    Text(
                        text = "Evaluation Name", /* TODO: Make it multi language */
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                    )
                },
                onValueChange = { evaluationName = it },
                maxLines = 1,
            )
            IconButton(onClick = { onEvaluationDelete(evaluation.evaluation.id) }) {
                Icon(Icons.Outlined.Close, contentDescription = "Delete Evaluation")
            }
        }
        OutlinedTextField(
            value = evaluationSubjectId,
            placeholder = {
                Text(
                    text = "Subject Id", /* TODO: Make it multi language */
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            onValueChange = { evaluationSubjectId = it },
            modifier = Modifier.fillMaxSize(),
            maxLines = 1,
        )
        gradeMarkWithColor(
            computeFinalMarkFromEvaluation(evaluation.listOfGrade),
            MaterialTheme.typography.headlineMedium
        )
        for (grade in evaluation.listOfGrade) {
            RacoGradeEditView(
                grade = grade,
                neededMark = 0.0,
                onGradeDelete = onGradeDelete,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center
        )
        {
            IconButton(onClick = { onGradeAdd(evaluation.evaluation.id) }) {
                Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Add Weight")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeView(
    grade: Grade,
    neededMark: Double
) {
    var gradeMark by remember { mutableStateOf(grade.mark) }
    var gradeWeight by remember { mutableStateOf(grade.weight) }
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = grade.name,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .width(150.dp)
        )
        Text(
            text = if (gradeWeight >= 0) "%.${2}f %".format(gradeWeight) else "NaN",
            style = MaterialTheme.typography.labelMedium
        )
        OutlinedTextField(
            value = if (gradeMark >= 0) "%.${2}f".format(gradeMark) else "",
            onValueChange = { gradeMark = it.toDouble() },
            placeholder = {
                Text(
                    text = neededMark.toString(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .width(100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeEditView(
    grade: Grade,
    neededMark: Double,
    onGradeDelete: (Int) -> Unit,
) {
    var gradeName by remember { mutableStateOf(grade.name) }
    var gradeMark by remember { mutableStateOf(grade.mark) }
    var gradeWeight by remember { mutableStateOf(grade.weight) }
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = gradeName,
            onValueChange = { gradeName = it },
            placeholder = {
                Text(
                    text = "Grade Name",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .width(150.dp),
            maxLines = 1,
        )
        OutlinedTextField(
            value = if (gradeWeight >= 0) "%.${2}f".format(gradeWeight) else "",
            onValueChange = { gradeWeight = it.toDouble() },
            placeholder = {
                Text(
                    text = "0.0 %",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .width(80.dp)
        )
        OutlinedTextField(
            value = if (gradeMark >= 0) "%.${2}f".format(gradeMark) else "",
            onValueChange = { gradeMark = it.toDouble() },
            placeholder = {
                Text(
                    text = neededMark.toString(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .width(70.dp)
        )
        IconButton(onClick = { onGradeDelete(grade.id) }) {
            Icon(Icons.Outlined.Close, contentDescription = "Delete Weight")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradesCollapsed(
    evaluation: EvaluationWithGrade,
    onGradeClick: (EvaluationWithGrade) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                onGradeClick(evaluation)
            },
        headlineText = {
            Text(
                text = evaluation.evaluation.name,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        supportingText = {
            gradeMarkWithColor(
                computeFinalMarkFromEvaluation(evaluation.listOfGrade),
                MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Composable
fun gradeMarkWithColor(
    mark: Double,
    style: TextStyle
) {
    Text(
        text = "%.${2}f".format(mark),
        style = style,
        color = when (mark) {
            in 0.0..4.9 -> MaterialTheme.colorScheme.error
            in 5.0..6.9 -> MaterialTheme.colorScheme.secondary
            in 7.0..10.0 -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }
    )
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