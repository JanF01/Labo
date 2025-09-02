package com.example.laboratorium


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laboratorium.ui.theme.LaboratoriumTheme
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview



class StationSelectedGradingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaboratoriumTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(Alignment.Top)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                actionContentColor = Color.Black,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                ) { innerPadding ->
                    StationSelectedGradingScreen(
                        modifier = Modifier.padding(innerPadding),
                        stationNumber = intent.getIntExtra("stationNumber", 0),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun StationSelectedGradingScreen(
    modifier: Modifier = Modifier,
    stationNumber: Int,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val students by StorageManager.getStudentsForStation(context, stationNumber).collectAsState(initial = emptyList())
    val tasks by StorageManager.getAllTasks(context).collectAsState(initial = emptyList())
    val passedTasks by StorageManager.getPassedTasks(context).collectAsState(initial = emptyMap())



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Stanowisko $stationNumber",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(students.sortedBy { it.albumNumber }) { student ->
                    // stan proponowanej oceny dla każdego studenta.
                    var editedProposedGradeText by rememberSaveable(student.albumNumber) {
                        mutableStateOf(student.proposedGrade.toString())
                    }
                    var isProposedGradeError by rememberSaveable(student.albumNumber) {
                        mutableStateOf(false)
                    }

                    // LaunchedEffect obserwuje zmiany w storageManager, jeżeli checkbox jest zaznaczony
                    // i zmieni się wartość w Storze to tutaj również się odświeży
                    LaunchedEffect(student.proposedGrade) {
                        if (editedProposedGradeText.toDoubleOrNull() != student.proposedGrade) {
                            editedProposedGradeText = student.proposedGrade.toString()
                            isProposedGradeError = false
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${student.name} ${student.surname}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nr albumu: ${student.albumNumber}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // List tasks for the student
                            tasks.sortedBy { it.grade }.forEach { task ->
                                val isTaskPassed = passedTasks[student.albumNumber]?.contains(task.description) ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isTaskPassed,
                                        onCheckedChange = { isChecked ->
                                            coroutineScope.launch {
                                                if (isChecked) {
                                                    StorageManager.markTaskAsPassed(context, student.albumNumber, task.description)
                                                } else {
                                                    StorageManager.markTaskAsNotPassed(
                                                        context,
                                                        student.albumNumber,
                                                        task.description
                                                    )
                                                }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = Color.White.copy(alpha = 0.8f)
                                        )
                                    )
                                    Text(
                                        text = "${task.description} (Ocena: ${task.grade})",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }


                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )


                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedTextField(
                                    value = editedProposedGradeText,
                                    onValueChange = { newValue ->
                                        editedProposedGradeText = newValue

                                        val grade = newValue.toDoubleOrNull()
                                        isProposedGradeError = if (grade == null) {
                                            true
                                        } else {
                                            !(grade >= 2.0 && grade <= 5.0)
                                        }
                                    },
                                    label = { Text("Proponowana Ocena") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    isError = isProposedGradeError,
                                    supportingText = {
                                        if (isProposedGradeError) {
                                            Text("Ocena musi być liczbą od 2.0 do 5.0", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        errorContainerColor = Color.Red.copy(alpha = 0.1f),
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                        errorLabelColor = MaterialTheme.colorScheme.error,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        errorTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val newGrade = editedProposedGradeText.toDoubleOrNull()
                                            if (newGrade != null && newGrade >= 2.0 && newGrade <= 5.0) {
                                                val updatedStudent = student.copy(proposedGrade = newGrade)
                                                StorageManager.updateStudent(context, student, updatedStudent)
                                                snackbarHostState.showSnackbar("Ocena studenta ${student.albumNumber} zaktualizowana na $newGrade", duration = SnackbarDuration.Short)
                                                isProposedGradeError = false
                                            } else {
                                                isProposedGradeError = true
                                                snackbarHostState.showSnackbar("Wprowadź poprawną ocenę (2.0-5.0).", duration = SnackbarDuration.Short)
                                            }
                                        }
                                    },
                                    enabled = !isProposedGradeError && editedProposedGradeText.toDoubleOrNull() != student.proposedGrade,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Zapisz Ocenę",
                                        tint = if (!isProposedGradeError && editedProposedGradeText.toDoubleOrNull() != student.proposedGrade)
                                            MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StationSelectedGradingScreenPreview() {
    LaboratoriumTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        StationSelectedGradingScreen(stationNumber = 1, snackbarHostState = snackbarHostState)
    }
}