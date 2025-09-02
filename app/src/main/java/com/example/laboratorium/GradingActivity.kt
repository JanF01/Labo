package com.example.laboratorium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laboratorium.ui.theme.LaboratoriumTheme
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class GradingActivity : ComponentActivity() {
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
                    GradingScreen(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GradingScreen(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var exportCompleted by remember { mutableStateOf(false) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                        StorageManager.exportToExcel(context, outputStream)
                        exportCompleted = true
                        snackbarHostState.showSnackbar("Dane wyeksportowane do Excela pomyślnie!", duration = SnackbarDuration.Long)
                    } ?: run {
                        snackbarHostState.showSnackbar("Nie udało się otworzyć strumienia wyjściowego.", duration = SnackbarDuration.Long)
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Błąd podczas eksportowania do Excela: ${e.localizedMessage}", duration = SnackbarDuration.Long)
                    e.printStackTrace()
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Eksport anulowany.", duration = SnackbarDuration.Short)
            }
        }
    }

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
                text = "Wybierz stanowisko",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            val stationNumbers = (1..10).toList()

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 5
            ) {
                stationNumbers.forEach { number ->
                    Button(
                        onClick = {
                            val intent = Intent(context, StationSelectedGradingActivity::class.java).apply {
                                putExtra("stationNumber", number)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .widthIn(min = 72.dp)
                            .weight(1f, fill = false)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "$number",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    val currentDateTime = Date()
                    val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
                    val formattedDateTime = formatter.format(currentDateTime)
                    createDocumentLauncher.launch("lista_${formattedDateTime}.xlsx")
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 32.dp, end = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eksportuj",
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Eksportuj do Excela",
                        tint = Color.White
                    )
                }
            }


            if (exportCompleted) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showConfirmClearDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text("Zakończ Laboratorium", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }


        if (showConfirmClearDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmClearDialog = false },
                title = { Text("Potwierdź", fontWeight = FontWeight.Bold) },
                text = { Text("Czy na pewno chcesz zakończyć laboratorium? Spowoduje to wyczyszczenie wszystkich danych.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmClearDialog = false
                            coroutineScope.launch {
                                StorageManager.clearAllData(context)
                                snackbarHostState.showSnackbar("Wszystkie dane zostały wyczyszczone. Powrót do ekranu głównego.", duration = SnackbarDuration.Long)
                                (context as? ComponentActivity)?.finish()
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("Tak")
                    }
                },
                dismissButton = {
                    Button(onClick = { showConfirmClearDialog = false }) {
                        Text("Nie")
                    }
                },
            )
        }
    }
}
