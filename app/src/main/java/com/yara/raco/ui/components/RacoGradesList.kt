package com.yara.raco.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject

@Composable
fun RacoEvalutaionList(
    evaluationWithGrade: List<EvaluationWithGrade>,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = evaluationWithGrade) { evaluationWithGrade ->
            SubjectGrades(
                evaluation = evaluationWithGrade
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectGrades(
    evaluation: EvaluationWithGrade
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }
    var showEditWeight by rememberSaveable { mutableStateOf(false) }
    var evaluationMark = computeFinalMarkFromEvaluation(evaluation.listOfGrade)
    OutlinedCard(
        border = CardDefaults.outlinedCardBorder(enabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        onClick = { showDetails = !showDetails }
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    text = evaluation.evaluation.subjectId,
                    style = MaterialTheme.typography.titleLarge
                )
                if (evaluationMark >= 5) Text(
                    text = evaluationMark.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                else Text(
                    text = evaluationMark.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            AnimatedVisibility(visible = showDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add Weight")
                        }
                        IconButton(onClick = { showEditWeight = !showEditWeight }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Weight")
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Weight")
                        }
                    }
                    if (evaluation.listOfGrade.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                        )
                        {
                            for (grade in evaluation.listOfGrade) {
                                ShowGrade(grade = grade, showEditWeight = showEditWeight)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowGrade(
    grade: Grade,
    showEditWeight: Boolean
) {
    Column(modifier = Modifier.padding(12.dp)) {
        var weight = grade.weight
        var mark = grade.mark
        Crossfade(
            targetState = showEditWeight
        ) { showEditWeight ->
            if (showEditWeight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = grade.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = grade.mark.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Weight")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = grade.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${grade.weight} %",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        Crossfade(targetState = showEditWeight) { showEditWeight ->
            if (showEditWeight) {
                OutlinedTextField(
                    weight.toString(),
                    onValueChange = { weight = it.toDouble() },
                    label = { Text("Weight") },
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    mark.toString(),
                    onValueChange = { mark = it.toDouble() },
                    label = { Text("Mark") },
                    singleLine = true
                )
            }
        }
    }
}

fun computeFinalMarkFromEvaluation(
    evaluation: List<Grade>
): Double {
    var mark = 0.0
    for (grade in evaluation) {
        mark += grade.mark * (grade.weight / 100)
    }
    return mark
}