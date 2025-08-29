package com.example.laboratorium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laboratorium.ui.theme.LaboratoriumTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextOverflow

class AddTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    AddTaskScreen(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
                }
            }
        }
}



@Composable
fun AddTaskScreen(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var taskDescription by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf<Double?>(null) }
    val grades = listOf(3.0, 3.5, 4.0, 4.5, 5.0)


    var editingTask by remember { mutableStateOf<Task?>(null) }


    val tasks by StorageManager.getAllTasks(context).collectAsState(initial = emptyList())


    LaunchedEffect(editingTask) {
        if (editingTask != null) {
            taskDescription = editingTask!!.description
            selectedGrade = editingTask!!.grade
        } else {
            taskDescription = ""
            selectedGrade = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (editingTask == null) "Dodaj Zadanie" else "Edytuj Zadanie",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text("Opis zadania") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Wybierz ocenę:",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades) { grade ->
                    Button(
                        onClick = { selectedGrade = grade },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGrade == grade) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.1f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$grade",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    coroutineScope.launch {
                        if (taskDescription.isBlank()) {
                            snackbarHostState.showSnackbar("Opis zadania nie może być pusty.")
                            return@launch
                        }

                        val gradeToSave = selectedGrade
                        if (gradeToSave == null) {
                            snackbarHostState.showSnackbar("Proszę wybrać ocenę.")
                            return@launch
                        }

                        val currentTaskDescription = taskDescription


                        val newTask = Task(currentTaskDescription, gradeToSave)

                        try {
                            taskDescription = ""
                            selectedGrade = null

                            if (editingTask == null) {

                                StorageManager.addTask(context, newTask)
                                snackbarHostState.showSnackbar("Zadanie zapisane pomyślnie!")
                            } else {

                                StorageManager.updateTask(context, editingTask!!, newTask)
                                snackbarHostState.showSnackbar("Zadanie zaktualizowane pomyślnie!")
                                editingTask = null
                            }


                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Błąd podczas zapisywania zadania.")
                            e.printStackTrace()
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) {
                Text(
                    text = if (editingTask == null) "Zapisz Zadanie" else "Zaktualizuj Zadanie",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            if (editingTask != null) {
                Button(
                    onClick = {
                        editingTask = null
                        taskDescription = ""
                        selectedGrade = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Anulowano edycję.")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                        .padding(top = 8.dp)
                ) {
                    Text("Anuluj Edycję", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            Text(
                text = "Lista Zadań",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (tasks.isEmpty()) {
                Text(
                    text = "Brak dodanych zadań.",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(tasks.sortedBy { it.grade } ) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.description,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Ocena: ${task.grade}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingTask = task
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edytuj zadanie",
                                            tint = Color.Yellow
                                        )
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            StorageManager.deleteTask(context, task)
                                            snackbarHostState.showSnackbar("Zadanie usunięte: ${task.description}")
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń zadanie",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, GradingActivity::class.java)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    "Zakończ Dodawanie Zadań",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskScreenPreview() {
    LaboratoriumTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        AddTaskScreen(snackbarHostState = snackbarHostState)
    }
}

