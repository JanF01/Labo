package com.example.laboratorium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.Saver
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

    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var albumNumber by rememberSaveable { mutableStateOf("") }
    var selectedStation by rememberSaveable { mutableStateOf<Int?>(null) }

    // śledzenie stanu studenta
    var studentToEdit by rememberSaveable(saver = StudentSaver) { mutableStateOf<Student?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(albumNumberToEdit) {
        if (albumNumberToEdit != null) {
            val allStudents = StorageManager.getAllStudents(context).first()
            val student = allStudents.find { it.albumNumber == albumNumberToEdit }

            if (student != null) {
                // repopulacja inputów przy obrocie ekranu
                name = student.name
                surname = student.surname
                albumNumber = student.albumNumber
                val assignments = StorageManager.getStationAssignments(context).first()
                selectedStation = assignments.entries.find { (_, albums) -> albums.contains(student.albumNumber) }?.key
            }
            studentToEdit = student // ustawienie studenta do edytowania
        } else {
            // Jeżeli dodawany jest nowy student resetuje inputy
            name = ""
            surname = ""
            albumNumber = ""
            selectedStation = null
            studentToEdit = null
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
            .padding(24.dp)
            .verticalScroll(scrollState),
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
                        if (studentToEdit == null) {

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

                            val originalStudentForUpdate = studentToEdit!!

                            StorageManager.updateStudent(context, originalStudentForUpdate, newOrUpdatedStudent)
                            StorageManager.assignStudentToStation(context, newOrUpdatedStudent.albumNumber, currentSelectedStation)
                            snackbarHostState.showSnackbar("Student zaktualizowany pomyślnie!", duration = SnackbarDuration.Short)


                            name = ""
                            surname = ""
                            albumNumber = ""
                            selectedStation = null
                            studentToEdit = null
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
                text = if (studentToEdit == null) "Dodaj Studenta" else "Zaktualizuj Studenta",
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

val StudentSaver = Saver<MutableState<Student?>, Any>(
    save = { state ->
        state.value?.let { student ->
            listOf(student.albumNumber, student.name, student.surname, student.proposedGrade)
        }
    },
    restore = { list ->
        // Cast the 'list' parameter to a List<Any> to enable array access
        val restoredList = list as? List<Any>
        if (restoredList != null &&
            restoredList.size == 4 &&
            restoredList[0] is String &&
            restoredList[1] is String &&
            restoredList[2] is String &&
            restoredList[3] is Double
        ) {
            mutableStateOf(
                Student(
                    albumNumber = restoredList[0] as String,
                    name = restoredList[1] as String,
                    surname = restoredList[2] as String,
                    proposedGrade = restoredList[3] as Double
                )
            )
        } else {
            mutableStateOf(null)
        }
    }
)

@Preview(showBackground = true)
@Composable
fun AddStudentScreenPreview() {
    LaboratoriumTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        AddStudentScreen(snackbarHostState = snackbarHostState)
    }
}
