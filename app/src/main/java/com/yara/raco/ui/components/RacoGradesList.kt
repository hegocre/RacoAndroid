package com.yara.raco.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yara.raco.R
import com.yara.raco.model.evaluation.EvaluationWithGrade
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.subject.Subject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditableEvaluationState(private val evaluationWithGrade: EvaluationWithGrade) {
    var subjectId by mutableStateOf(evaluationWithGrade.evaluation.subjectId)
    var evaluationName by mutableStateOf(evaluationWithGrade.evaluation.name)
    val gradesList = evaluationWithGrade.listOfGrade.sortedBy { it.name }.toMutableStateList()

    fun getEvaluationWithGrades(): EvaluationWithGrade = EvaluationWithGrade(
        evaluation = evaluationWithGrade.evaluation.copy(
            subjectId = subjectId,
            name = evaluationName
        ),
        listOfGrade = gradesList
    )

    companion object {
        val Saver: Saver<EditableEvaluationState, *> = listSaver(
            save = { listOf(Json.encodeToString(it.getEvaluationWithGrades())) },
            restore = { EditableEvaluationState(Json.decodeFromString(it[0])) }
        )
    }
}

@Composable
fun rememberEditableEvaluationState(evaluationWithGrade: EvaluationWithGrade): EditableEvaluationState =
    rememberSaveable(evaluationWithGrade, saver = EditableEvaluationState.Saver) {
        EditableEvaluationState(evaluationWithGrade)
    }

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun RacoGradesList(
    subjects: List<Subject>,
    evaluations: List<EvaluationWithGrade>,
    onGradeClick: (EvaluationWithGrade) -> Unit,
    onAddEvaluationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectsIds = (evaluations.map { it.evaluation.subjectId }).distinct()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvaluationClick) {
                Icon(Icons.Outlined.Add, contentDescription = "Add grade")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.padding(paddingValues)
        ) {
            for (subject in subjectsIds) {
                val subjectName = subjects.find { it.sigles == subject }?.nom
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (subject != "") subjectName.toString() else "Unlabeled",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                itemsIndexed(
                    items = evaluations.filter { it.evaluation.subjectId == subject },
                    key = { _, evaluation -> evaluation.evaluation.id }
                ) { index, evaluation ->
                    if (index != 0) {
                        Divider()
                    }
                    RacoGradesCollapsed(
                        evaluation = evaluation,
                        onGradeClick = onGradeClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedEvaluationWithGradeCall(
    subjects: List<Subject>,
    evaluation: EvaluationWithGrade,
    onEvaluationUpdate: (EvaluationWithGrade) -> Unit,
    onEvaluationDelete: (Int) -> Unit,
) {
    var isEditingEvaluation by remember { mutableStateOf(false) }

    val editableEvaluationState = rememberEditableEvaluationState(evaluationWithGrade = evaluation)

    val neededMark by remember {
        derivedStateOf {
            computeAverageMarkForPassing(editableEvaluationState.gradesList)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditingEvaluation) {
                        editableEvaluationState.gradesList.filter { it.id < 0 }
                            .forEach { it.id = 0 }
                        onEvaluationUpdate(editableEvaluationState.getEvaluationWithGrades())
                    }
                    isEditingEvaluation = !isEditingEvaluation
                }
            ) {
                Icon(
                    if (!isEditingEvaluation) Icons.Outlined.Edit else Icons.Outlined.Save,
                    contentDescription = "Add grade"
                )
            }
        }
    ) { paddingValues ->
        BackHandler(onBack = { isEditingEvaluation = false }, enabled = isEditingEvaluation)

        Crossfade(targetState = isEditingEvaluation) { editing ->
            if (!editing) {
                DetailedEvaluationWithGrade(
                    evaluation = editableEvaluationState.getEvaluationWithGrades(),
                    neededMark = neededMark,
                    modifier = Modifier.padding(paddingValues),
                    onGradeUpdate = { newGrade ->
                        editableEvaluationState.gradesList[editableEvaluationState.gradesList.indexOfFirst { it.id == newGrade.id }] =
                            newGrade
                    }
                )
                DisposableEffect(LocalLifecycleOwner.current) {
                    onDispose {
                        onEvaluationUpdate(editableEvaluationState.getEvaluationWithGrades())
                    }
                }
            } else {
                EditEvaluationWithGrade(
                    subjects = subjects,
                    editableEvaluationState = editableEvaluationState,
                    onGradeAdd = { grade ->
                        editableEvaluationState.gradesList.add(
                            grade.copy(
                                evaluationId = editableEvaluationState.getEvaluationWithGrades().evaluation.id
                            )
                        )
                    },
                    onGradeDelete = { grade -> editableEvaluationState.gradesList.remove(grade) },
                    onGradeUpdate = { grade ->
                        editableEvaluationState.gradesList[editableEvaluationState.gradesList.indexOfFirst { it.id == grade.id }] =
                            grade
                    },
                    onEvaluationDelete = { onEvaluationDelete(editableEvaluationState.getEvaluationWithGrades().evaluation.id) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun DetailedEvaluationWithGrade(
    evaluation: EvaluationWithGrade,
    onGradeUpdate: (Grade) -> Unit,
    neededMark: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        LazyColumn {
            item {
                ElevatedCard(modifier = Modifier) {
                    Row(
                        modifier = Modifier.padding(all = 24.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = evaluation.evaluation.name,
                                style = MaterialTheme.typography.labelLarge
                            )

                            Text(
                                text = evaluation.evaluation.subjectId,
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }

                        GradeMarkWithColor(
                            computeFinalMarkFromEvaluation(evaluation.listOfGrade),
                            MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.grades),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            items(items = evaluation.listOfGrade) { grade ->
                Spacer(modifier = Modifier.height(8.dp))
                RacoGradeView(
                    grade = grade,
                    neededMark = neededMark,
                    onMarkUpdate = { newMark ->
                        onGradeUpdate(grade.copy(mark = newMark))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationWithGrade(
    subjects: List<Subject>,
    editableEvaluationState: EditableEvaluationState,
    onGradeAdd: (Grade) -> Unit,
    onGradeDelete: (Grade) -> Unit,
    onGradeUpdate: (Grade) -> Unit,
    onEvaluationDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectsMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .clickable { subjectsMenuExpanded = true },
        ) {
            Row(verticalAlignment = CenterVertically) {
                Text(
                    text = subjects.find { it.id == editableEvaluationState.subjectId }?.nom ?: "",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { subjectsMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = ""
                    )
                }
            }
            DropdownMenu(
                expanded = subjectsMenuExpanded,
                onDismissRequest = { subjectsMenuExpanded = false },
            ) {
                subjects.forEach { subject ->
                    DropdownMenuItem(text = { Text(text = subject.nom) }, onClick = {
                        editableEvaluationState.subjectId = subject.id
                        subjectsMenuExpanded = false
                    })
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            value = editableEvaluationState.evaluationName,
            onValueChange = { newValue -> editableEvaluationState.evaluationName = newValue },
            singleLine = true,
            maxLines = 1,
            placeholder = {
                Text(text = stringResource(id = R.string.evaluation_name))
            }
        )

        Text(
            text = stringResource(id = R.string.grades),
            style = MaterialTheme.typography.titleMedium
        )

        for (grade in editableEvaluationState.gradesList) {
            RacoGradeEditView(
                grade = grade,
                onGradeUpdate = onGradeUpdate,
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
            IconButton(
                onClick = {
                    val newGrade = Grade(
                        id = -(System.currentTimeMillis().toInt() % 100),
                        name = "",
                        mark = null,
                        weight = 0.0,
                        evaluationId = 0
                    )
                    onGradeAdd(newGrade)
                }
            ) {
                Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Add Weight")
            }
        }

        Button(
            onClick = { onEvaluationDelete() },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.delete_evaluation))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeView(
    grade: Grade,
    onMarkUpdate: (Double?) -> Unit,
    neededMark: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = grade.name,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = grade.mark?.toString() ?: "",
            onValueChange = { newMark -> onMarkUpdate(newMark.toDoubleOrNull() ?: grade.mark) },
            placeholder = {
                Text(
                    text = String.format("%.2f", neededMark)
                )
            },
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoGradeEditView(
    grade: Grade,
    onGradeUpdate: (Grade) -> Unit,
    onGradeDelete: (Grade) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = grade.name,
            onValueChange = { onGradeUpdate(grade.copy(name = it)) },
            placeholder = {
                Text(
                    text = "Grade Name",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            maxLines = 1,
        )
        OutlinedTextField(
            value = grade.weight.toString(),
            onValueChange = {
                onGradeUpdate(
                    grade.copy(
                        weight = it.toDoubleOrNull() ?: grade.weight
                    )
                )
            },
            placeholder = {
                Text(
                    text = "0.0 %",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .width(80.dp),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        IconButton(onClick = { onGradeDelete(grade) }) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete Weight")
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
            GradeMarkWithColor(
                computeFinalMarkFromEvaluation(evaluation.listOfGrade),
                MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Composable
fun GradeMarkWithColor(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvaluationDialog(
    subjects: List<Subject>,
    onAddClick: (String, String) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    val (subjectId, setSubjectId) = remember { mutableStateOf(subjects.first().id) }
    val (evaluationName, setEvaluationName) = remember { mutableStateOf("") }

    var subjectsMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.animateContentSize()
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = stringResource(id = R.string.add_evaluation),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .clickable { subjectsMenuExpanded = true },
                ) {
                    Row(verticalAlignment = CenterVertically) {
                        Text(
                            text = subjects.find { it.id == subjectId }?.nom ?: "",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { subjectsMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = ""
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = subjectsMenuExpanded,
                        onDismissRequest = { subjectsMenuExpanded = false },
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(text = { Text(text = subject.nom) }, onClick = {
                                setSubjectId(subject.id)
                                subjectsMenuExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp),
                    value = evaluationName,
                    onValueChange = setEvaluationName,
                    singleLine = true,
                    maxLines = 1,
                    placeholder = {
                        Text(text = stringResource(id = R.string.evaluation_name))
                    }
                )

                TextButton(
                    onClick = { onAddClick(subjectId, evaluationName) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 0.dp)
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        }
    }
}

fun computeFinalMarkFromEvaluation(
    evaluation: List<Grade>
): Double {
    var mark = 0.0
    for (grade in evaluation) {
        grade.mark?.let {
            mark += it * (grade.weight / 100)
        }
    }
    return mark
}

fun computeAverageMarkForPassing(
    evaluation: List<Grade>
): Double {
    var remainingWeight = 0.0
    for (grade in evaluation) {
        if (grade.mark == null) {
            remainingWeight += grade.weight
        }
    }
    val currentMark = computeFinalMarkFromEvaluation(evaluation)

    return (5.0 - currentMark) / (remainingWeight / 100).coerceIn(0.0, 10.0)
}