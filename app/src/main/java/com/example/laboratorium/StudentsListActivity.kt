package com.example.laboratorium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Intent


import com.example.laboratorium.ui.theme.LaboratoriumTheme

class StudentsListActivity : ComponentActivity() {
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
                    StudentsListScreen(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}




@Composable
fun StudentsListScreen(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val studentsFlow = StorageManager.getAllStudents(context)
    val assignmentsFlow = StorageManager.getStationAssignments(context)

    val students by studentsFlow.collectAsState(initial = emptyList())
    val assignments by assignmentsFlow.collectAsState(initial = emptyMap())


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Lista Przypisanych Studentów",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val sortedAssignments = assignments.toList().sortedBy { (station, _) -> station }

            LazyColumn(
                modifier = Modifier
                    .weight(1f) ,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sortedAssignments.forEach { (station, assignedAlbumNumbers) ->
                    item {
                        Text(
                            text = "Stacja $station",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(assignedAlbumNumbers) { album ->
                        val student = students.find { it.albumNumber == album }
                        if (student != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${student.name} ${student.surname} (${student.albumNumber})",
                                    color = Color.White
                                )
                                Row {
                                    IconButton(onClick = {
                                        val intent =
                                            Intent(context, AddStudentActivity::class.java).apply {
                                                putExtra("albumNumberToEdit", student.albumNumber)
                                            }
                                        context.startActivity(intent)
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color.Yellow
                                        )
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            StorageManager.deleteStudent(
                                                context,
                                                student.albumNumber
                                            )
                                            snackbarHostState.showSnackbar(
                                                "Student ${student.albumNumber} został usunięty.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
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
                onClick = { showConfirmDialog = true },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    "Zatwierdź",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }


    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },

            title = {
                    Text(
                        "Potwierdź",
                        fontWeight = FontWeight.Bold,
                    )
            },
            text = {
                    Text(
                        "Czy na pewno chcesz zatwierdzić listę i przejść do dodawania zadań?",
                    )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val intent = Intent(context, AddTaskActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {

                    Text("Tak")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Nie")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentsListScreenPreview() {
    LaboratoriumTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        StudentsListScreen(snackbarHostState = snackbarHostState)
    }
}