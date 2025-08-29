package com.example.laboratorium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.content.Context

import com.example.laboratorium.ui.theme.LaboratoriumTheme

class AddStudentActivity : ComponentActivity() {
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
                    val albumNumberToEdit = intent.getStringExtra("albumNumberToEdit")


                    AddStudentScreen(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState,
                        albumNumberToEdit = albumNumberToEdit,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStudentScreen(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState, albumNumberToEdit: String? = null) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val studentToEditState = remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(albumNumberToEdit) {
        if (albumNumberToEdit != null) {
            val allStudents = StorageManager.getAllStudents(context).first()
            studentToEditState.value = allStudents.find { it.albumNumber == albumNumberToEdit }
        } else {
            studentToEditState.value = null
        }
    }

    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var albumNumber by rememberSaveable { mutableStateOf("") }
    var selectedStation by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(studentToEditState.value) {
        val student = studentToEditState.value
        if (student != null) {
            name = student.name
            surname = student.surname
            albumNumber = student.albumNumber
            val assignments = StorageManager.getStationAssignments(context).first()
            selectedStation = assignments.entries.find { (_, albums) -> albums.contains(student.albumNumber) }?.key
        } else {

            name = ""
            surname = ""
            albumNumber = ""
            selectedStation = null
        }
    }


    val stations = (1..10).toList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Dodaj Nowego Studenta",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Imię") },
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

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Nazwisko") },
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

        OutlinedTextField(
            value = albumNumber,
            onValueChange = { albumNumber = it },
            label = { Text("Numer albumu") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            text = "Wybierz stację:",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stations.forEach { stationNumber ->
                Button(
                    onClick = { selectedStation = stationNumber },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStation == stationNumber) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(70.dp)
                ) {
                    Text(
                        text = "$stationNumber",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    if (name.isBlank() || surname.isBlank() || albumNumber.isBlank() || selectedStation == null) {
                        snackbarHostState.showSnackbar(
                            "Wypełnij wszystkie pola i wybierz stację.",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }

                    val currentAlbumNumber = albumNumber
                    val currentName = name
                    val currentSurname = surname
                    val currentSelectedStation = selectedStation!!

                    val newOrUpdatedStudent = Student(
                        albumNumber = currentAlbumNumber,
                        name = currentName,
                        surname = currentSurname
                    )

                    try {
                        if (studentToEditState.value == null) {

                            name = ""
                            surname = ""
                            albumNumber = ""
                            selectedStation = null

                            val existingStudents = StorageManager.getAllStudents(context).first()
                            if (existingStudents.none { it.albumNumber == newOrUpdatedStudent.albumNumber }) {
                                StorageManager.addStudent(context, newOrUpdatedStudent)
                                StorageManager.assignStudentToStation(
                                    context,
                                    newOrUpdatedStudent.albumNumber,
                                    currentSelectedStation
                                )
                                snackbarHostState.showSnackbar(
                                    "Student dodany pomyślnie!",
                                    duration = SnackbarDuration.Short
                                )

                            } else {
                                snackbarHostState.showSnackbar(
                                    "Student o tym numerze albumu już istnieje.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {

                            val originalStudentForUpdate = studentToEditState.value!!

                            StorageManager.updateStudent(context, originalStudentForUpdate, newOrUpdatedStudent)
                            StorageManager.assignStudentToStation(context, newOrUpdatedStudent.albumNumber, currentSelectedStation)
                            snackbarHostState.showSnackbar("Student zaktualizowany pomyślnie!", duration = SnackbarDuration.Short)


                            name = ""
                            surname = ""
                            albumNumber = ""
                            selectedStation = null
                            studentToEditState.value = null
                            (context as? ComponentActivity)?.finish()
                        }

                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            "Błąd podczas zapisywania danych.",
                            duration = SnackbarDuration.Short
                        )
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
                text = if (studentToEditState.value == null) "Dodaj Studenta" else "Zaktualizuj Studenta",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    FloatingActionButton(
        onClick = {
            val intent = Intent(context, StudentsListActivity::class.java)
            context.startActivity(intent)
        },
        containerColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp)
    ) {
        Text("Zakończ", color = Color.White)
    }
    }
}



@Preview(showBackground = true)
@Composable
fun AddStudentScreenPreview() {
    LaboratoriumTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        AddStudentScreen(snackbarHostState = snackbarHostState)
    }
}
