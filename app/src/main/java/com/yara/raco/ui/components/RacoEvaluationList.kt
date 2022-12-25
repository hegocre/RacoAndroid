package com.yara.raco.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yara.raco.R
import com.yara.raco.model.evaluation.EvaluationWithGrades
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.grade.MutableGrade
import com.yara.raco.model.subject.Subject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditableEvaluationState(private val evaluationWithGrades: EvaluationWithGrades) {
    private constructor(
        evaluationWithGrades: EvaluationWithGrades,
        evaluationName: String,
        gradesList: List<MutableGrade>
    ) : this(evaluationWithGrades) {
        this.evaluationName = evaluationName
        this.gradesList.clear()
        this.gradesList.addAll(gradesList)
    }

    val evaluationId = evaluationWithGrades.evaluation.id
    var evaluationName by mutableStateOf(evaluationWithGrades.evaluation.name)
    val gradesList = evaluationWithGrades.listOfGrade.sortedBy { it.name }.map {
        MutableGrade(
            id = it.id,
            name = it.name,
            weight = it.weight.toString(),
            mark = (it.mark ?: "").toString(),
            evaluationId = it.evaluationId
        )
    }.toMutableStateList()

    fun getEvaluationWithGrades(): EvaluationWithGrades = EvaluationWithGrades(
        evaluation = evaluationWithGrades.evaluation.copy(
            name = evaluationName
        ),
        listOfGrade = gradesList.map {
            it.toGrade()
        }
    )

    companion object {
        val Saver: Saver<EditableEvaluationState, *> = listSaver(
            save = {
                listOf(Json.encodeToString(it.evaluationWithGrades))
                    .plus(it.evaluationName)
                    .plus(Json.encodeToString(it.gradesList.toList()))
            },
            restore = {
                EditableEvaluationState(
                    evaluationWithGrades = Json.decodeFromString(it[0]),
                    evaluationName = it[1],
                    gradesList = Json.decodeFromString(it[2])
                )
            }
        )
    }
}

@Composable
fun rememberEditableEvaluationState(evaluationWithGrades: EvaluationWithGrades): EditableEvaluationState =
    rememberSaveable(evaluationWithGrades, saver = EditableEvaluationState.Saver) {
        EditableEvaluationState(evaluationWithGrades)
    }

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun RacoEvaluationList(
    subjects: List<Subject>,
    evaluations: List<EvaluationWithGrades>,
    onGradeClick: (EvaluationWithGrades) -> Unit,
    onAddEvaluationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvaluationClick) {
                Icon(Icons.Outlined.Add, contentDescription = "Add grade")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val filteredEvaluations = remember(evaluations, subjects) {
            subjects.associate { subject ->
                Pair(
                    subject.id,
                    evaluations.filter { it.evaluation.subjectId == subject.id })
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            for (subject in subjects) {
                val evaluationsList = filteredEvaluations.getOrDefault(subject.id, emptyList())

                if (evaluationsList.isNotEmpty()) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = subject.nom,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    itemsIndexed(
                        items = evaluationsList,
                        key = { _, evaluation -> evaluation.evaluation.id }
                    ) { index, evaluation ->
                        if (index != 0) {
                            Divider()
                        }
                        EvaluationListEntry(
                            evaluation = evaluation,
                            onGradeClick = onGradeClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailedEvaluation(
    evaluation: EvaluationWithGrades,
    onEditClick: () -> Unit,
    onGradeUpdate: (Grade) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEditClick
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = stringResource(id = R.string.edit)
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val sortedGrades by remember {
            derivedStateOf { evaluation.listOfGrade.sortedBy { it.name } }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumedWindowInsets(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item(key = "evaluation_header") {
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
                            mark = evaluation.getFinalMark(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.grades),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            items(
                items = sortedGrades,
                key = { grade -> grade.id }
            ) { grade ->
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableGradeMark(
                        grade = grade,
                        neededMark = evaluation.getPassMark(),
                        onMarkUpdate = { newMark ->
                            onGradeUpdate(grade.copy(mark = newMark))
                        }
                    )
                }
            }

            //Workaround, see https://stackoverflow.com/questions/73894748/compose-how-to-have-ime-padding-and-scaffold-padding-with-edge-to-edge-and-wind
            item(key = "ime_spacer") {
                Spacer(modifier = Modifier.imePadding())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditableEvaluation(
    editableEvaluationState: EditableEvaluationState,
    onEvaluationSave: () -> Unit,
    onEvaluationDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEvaluationSave,
            ) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumedWindowInsets(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "evaluation_name") {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    value = editableEvaluationState.evaluationName,
                    onValueChange = { newValue ->
                        editableEvaluationState.evaluationName = newValue
                    },
                    singleLine = true,
                    maxLines = 1,
                    placeholder = {
                        Text(text = stringResource(id = R.string.evaluation_name))
                    }
                )
            }

            item(key = "grades_title") {
                Text(
                    text = stringResource(id = R.string.grades),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            itemsIndexed(
                items = editableEvaluationState.gradesList,
                key = { index, evaluation -> evaluation.name + index }
            ) { index, grade ->
                EditableGradeWeight(
                    grade = grade,
                    onNameChange = { newName ->
                        editableEvaluationState.gradesList[index] =
                            editableEvaluationState.gradesList[index].copy(name = newName)
                    },
                    onWeightChange = { newWeight ->
                        editableEvaluationState.gradesList[index] =
                            editableEvaluationState.gradesList[index].copy(weight = newWeight)
                    },
                    onGradeDelete = { editableEvaluationState.gradesList.removeAt(index) },
                )
            }

            item(key = "new_grade_button") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            val newGrade = MutableGrade(
                                id = null,
                                name = "",
                                mark = "",
                                weight = "0.0",
                                evaluationId = editableEvaluationState.evaluationId
                            )
                            editableEvaluationState.gradesList.add(newGrade)
                        },
                        modifier = Modifier.align(CenterHorizontally)
                    ) {
                        Icon(
                            Icons.Outlined.AddCircleOutline,
                            contentDescription = stringResource(id = R.string.add_grade)
                        )
                    }
                }
            }

            item(key = "delete_evaluation_button") {
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

            //Workaround, see https://stackoverflow.com/questions/73894748/compose-how-to-have-ime-padding-and-scaffold-padding-with-edge-to-edge-and-wind
            item(key = "ime_spacer") {
                Spacer(modifier = Modifier.imePadding())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableGradeMark(
    grade: Grade,
    onMarkUpdate: (Double?) -> Unit,
    neededMark: Double
) {
    val (editableMark, setEditableMark) = rememberSaveable {
        mutableStateOf(
            (grade.mark ?: "").toString()
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = grade.name,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = editableMark,
            onValueChange = { newMark ->
                setEditableMark(newMark)
                if (newMark == "") {
                    onMarkUpdate(null)
                } else if (newMark.toDoubleOrNull() != null) {
                    onMarkUpdate(newMark.toDoubleOrNull())
                }
            },
            placeholder = {
                Text(
                    text = String.format("%.2f", neededMark)
                )
            },
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = editableMark != "" && editableMark.toDoubleOrNull() == null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableGradeWeight(
    grade: MutableGrade,
    onNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onGradeDelete: (MutableGrade) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = grade.name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.grade_name),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)
                )
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            maxLines = 1,
            singleLine = true
        )
        OutlinedTextField(
            value = grade.weight,
            onValueChange = onWeightChange,
            modifier = Modifier
                .width(120.dp),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Icon(imageVector = Icons.Default.Percent, contentDescription = "")
            },
            isError = grade.weight != "" && grade.weight.toDoubleOrNull() == null
        )
        IconButton(onClick = { onGradeDelete(grade) }) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(id = R.string.delete_grade)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationListEntry(
    evaluation: EvaluationWithGrades,
    onGradeClick: (EvaluationWithGrades) -> Unit,
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
                evaluation.getFinalMark(),
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
            in 7.0..10.0 -> MaterialTheme.colorScheme.primary
            in 5.0..7.0 -> MaterialTheme.colorScheme.secondary
            in 0.0..5.0 -> MaterialTheme.colorScheme.error
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

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
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
                                    contentDescription = null
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
                }


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