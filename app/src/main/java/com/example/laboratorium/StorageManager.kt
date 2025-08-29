package com.example.laboratorium

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook // Import for Excel workbook
import android.util.Log // Import for logging

data class Student(val albumNumber: String, val name: String, val surname: String, var proposedGrade: Double = 2.0)
data class Task(val description: String, val grade: Double)

typealias StationAssignments = MutableMap<Int, MutableList<String>>

typealias PassedTasks = MutableMap<String, MutableSet<String>>

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "persistent_data")

object StorageManager {

    private val GSON = Gson()

    private val ALL_STUDENTS_KEY = stringPreferencesKey("all_students")
    private val ALL_TASKS_KEY = stringPreferencesKey("all_tasks")
    private val STATION_ASSIGNMENTS_KEY = stringPreferencesKey("station_assignments")

    private val PASSED_TASKS_KEY = stringPreferencesKey("passed_tasks")




    /**
     * Dodaje nowego studenta do listy studentów
     */
    suspend fun addStudent(context: Context, student: Student) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_STUDENTS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Student>>() {}.type
            val students: MutableList<Student> = GSON.fromJson(json, type)
            students.add(student)
            prefs[ALL_STUDENTS_KEY] = GSON.toJson(students)
        }
    }

    /**
     * Usuwa studenta z listy studentów zależnie od numeru albumu
     */
    suspend fun deleteStudent(context: Context, albumNumber: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_STUDENTS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Student>>() {}.type
            val students: MutableList<Student> = GSON.fromJson(json, type)
            val updatedStudents = students.filter { it.albumNumber != albumNumber }
            prefs[ALL_STUDENTS_KEY] = GSON.toJson(updatedStudents)
        }
    }

    /**
     * Aktualizuj istniejącego studenta.
     */
    suspend fun updateStudent(context: Context, originalStudent: Student, updatedStudent: Student) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_STUDENTS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Student>>() {}.type
            val students: MutableList<Student> = GSON.fromJson(json, type)

            val filteredStudents = students.filter { it.albumNumber != originalStudent.albumNumber }
            val updatedStudentsList = (filteredStudents + updatedStudent).toMutableList()

            prefs[ALL_STUDENTS_KEY] = GSON.toJson(updatedStudentsList)
        }
    }

    /**
     * Uzyskaj liste wszystkich studentów
     */
    fun getAllStudents(context: Context): Flow<List<Student>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[ALL_STUDENTS_KEY] ?: "[]"
            val type = object : TypeToken<List<Student>>() {}.type
            GSON.fromJson(json, type)
        }
    }

    /**
     * Przypisanie studenta do stanowiska
     */
    suspend fun assignStudentToStation(context: Context, albumNumber: String, stationNumber: Int) {
        context.dataStore.edit { prefs ->
            val json = prefs[STATION_ASSIGNMENTS_KEY] ?: "{}"
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            val assignmentsRaw: Map<String, List<String>> = try {
                GSON.fromJson(json, type) ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }

            val assignments: StationAssignments =
                assignmentsRaw.mapKeys { it.key.toInt() }
                    .mapValues { it.value.toMutableList() }
                    .toMutableMap()

            assignments.values.forEach { it.remove(albumNumber) }

            val studentsForStation = assignments.getOrPut(stationNumber) { mutableListOf() }
            if (!studentsForStation.contains(albumNumber)) {
                studentsForStation.add(albumNumber)
            }

            prefs[STATION_ASSIGNMENTS_KEY] = GSON.toJson(assignments)
        }
    }


    /**
     * Uzyskanie listy przypisań
     */
    fun getStationAssignments(context: Context): Flow<Map<Int, List<String>>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[STATION_ASSIGNMENTS_KEY] ?: "{}"
            val type = object : TypeToken<StationAssignments>() {}.type
            GSON.fromJson(json, type)
        }
    }

    /**
     * Uzyskanie listy studentów przypisaanych do danego stanowiska
     */
    fun getStudentsForStation(context: Context, stationNumber: Int): Flow<List<Student>> {
        // Kombinacja studentów oraz przypisań
        return getAllStudents(context).combine(getStationAssignments(context)) { allStudents, assignments ->
            // Uzyskanie listy albumów dla danego stanowiska
            val assignedAlbumNumbers = assignments[stationNumber] ?: emptyList()
            // Filter studentów
            allStudents.filter { assignedAlbumNumbers.contains(it.albumNumber) }
        }
    }

    /**
     * Ustawienie zadania jako zaliczone przez studenta i aktualizacja proponowanej oceny
     */
    suspend fun markTaskAsPassed(context: Context, albumNumber: String, taskDescription: String) {
        context.dataStore.edit { prefs ->
            val passedTasksJson = prefs[PASSED_TASKS_KEY] ?: "{}"
            val passedTasksType = object : TypeToken<PassedTasks>() {}.type
            val passedTasks: PassedTasks = GSON.fromJson(passedTasksJson, passedTasksType)

            val studentTasks = passedTasks.getOrPut(albumNumber) { mutableSetOf() }
            studentTasks.add(taskDescription)
            prefs[PASSED_TASKS_KEY] = GSON.toJson(passedTasks)

            val allStudents = getAllStudents(context).first().toMutableList()
            val studentToUpdate = allStudents.find { it.albumNumber == albumNumber }
            val task = getAllTasks(context).first().find { it.description == taskDescription }

            if (studentToUpdate != null && task != null) {
                if (task.grade > studentToUpdate.proposedGrade) {
                    val updatedStudent = studentToUpdate.copy(proposedGrade = task.grade)

                    val index = allStudents.indexOfFirst { it.albumNumber == albumNumber }
                    if (index != -1) {
                        allStudents[index] = updatedStudent
                        prefs[ALL_STUDENTS_KEY] = GSON.toJson(allStudents)
                    }
                }
            }
        }
    }

    /**
     * Ustawienie zadania jako nie zaliczone przez studenta i ponowne przeliczenie proponowanej oceny
     */
    suspend fun markTaskAsNotPassed(context: Context, albumNumber: String, taskDescription: String) {
        context.dataStore.edit { prefs ->
            val passedTasksJson = prefs[PASSED_TASKS_KEY] ?: "{}"
            val passedTasksType = object : TypeToken<PassedTasks>() {}.type
            val passedTasks: PassedTasks = GSON.fromJson(passedTasksJson, passedTasksType)

            val studentTasks = passedTasks[albumNumber]
            studentTasks?.remove(taskDescription)
            prefs[PASSED_TASKS_KEY] = GSON.toJson(passedTasks)

            // Rekalkulacja proponowanej oceny
            val allStudents = getAllStudents(context).first().toMutableList()
            val studentToUpdate = allStudents.find { it.albumNumber == albumNumber }

            if (studentToUpdate != null) {
                val allAvailableTasks = getAllTasks(context).first()
                val remainingPassedTaskDescriptions = passedTasks[albumNumber] ?: emptySet()

                val remainingPassedTasksWithGrades = allAvailableTasks
                    .filter { task -> remainingPassedTaskDescriptions.contains(task.description) }

                val newProposedGrade = if (remainingPassedTasksWithGrades.isNotEmpty()) {
                    remainingPassedTasksWithGrades.maxOf { it.grade }
                } else {
                    2.0
                }

                if (newProposedGrade != studentToUpdate.proposedGrade) {
                    val updatedStudent = studentToUpdate.copy(proposedGrade = newProposedGrade)
                    val index = allStudents.indexOfFirst { it.albumNumber == albumNumber }
                    if (index != -1) {
                        allStudents[index] = updatedStudent
                        prefs[ALL_STUDENTS_KEY] = GSON.toJson(allStudents)
                    }
                }
            }
        }
    }

    fun getPassedTasks(context: Context): Flow<PassedTasks> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[PASSED_TASKS_KEY] ?: "{}"
            val type = object : TypeToken<PassedTasks>() {}.type
            GSON.fromJson(json, type) ?: mutableMapOf()
        }
    }

    /**
     * Dodanie nowego zadania
     */
    suspend fun addTask(context: Context, task: Task) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_TASKS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Task>>() {}.type
            val tasks: MutableList<Task> = GSON.fromJson(json, type)
            tasks.add(task)
            prefs[ALL_TASKS_KEY] = GSON.toJson(tasks)
        }
    }

    /**
     * Usuń zadanie z listy
     */
    suspend fun deleteTask(context: Context, taskToDelete: Task) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_TASKS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Task>>() {}.type
            val tasks: MutableList<Task> = GSON.fromJson(json, type)
            val updatedTasks = tasks.filter { it.description != taskToDelete.description || it.grade != taskToDelete.grade }
            prefs[ALL_TASKS_KEY] = GSON.toJson(updatedTasks)
        }
    }


    /**
     * Uzyskanie listy zadań
     */
    fun getAllTasks(context: Context): Flow<List<Task>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[ALL_TASKS_KEY] ?: "[]"
            val type = object : TypeToken<List<Task>>() {}.type
            GSON.fromJson(json, type)
        }
    }

    /**
     * Aktualizuj istniejące zadanie.
     */
    suspend fun updateTask(context: Context, originalTask: Task, updatedTask: Task) {
        context.dataStore.edit { prefs ->
            val json = prefs[ALL_TASKS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<Task>>() {}.type
            val tasks: MutableList<Task> = GSON.fromJson(json, type)

            val filteredTasks = tasks.filter { it.description != originalTask.description || it.grade != originalTask.grade }
            val updatedTasksList = (filteredTasks + updatedTask).toMutableList()

            prefs[ALL_TASKS_KEY] = GSON.toJson(updatedTasksList)
        }
    }

    /**
     * Usunięcie danych
     */
    suspend fun clearAllData(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Eksportuje dane studentów, ich przypisań do stacji i zaliczonych zadań do pliku Excel.
     */
    suspend fun exportToExcel(context: Context, outputStream: OutputStream) {
        val allStudents = getAllStudents(context).first()
        val allTasks = getAllTasks(context).first()
        val stationAssignments = getStationAssignments(context).first()
        val passedTasks = getPassedTasks(context).first()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Raport Ocen")

        var currentRowIndex = 0

        val sortedStations = stationAssignments.keys.sorted()

        sortedStations.forEach { stationNumber ->
            // --- Station Header ---
            val stationHeaderRow = sheet.createRow(currentRowIndex++)
            val stationHeaderCell = stationHeaderRow.createCell(0)
            stationHeaderCell.setCellValue("Stacja $stationNumber")

            currentRowIndex++ // Add a blank row after station header for separation

            val currentStationContentStartRow = currentRowIndex // Mark where student data for this station begins

            val assignedAlbumNumbers = stationAssignments[stationNumber] ?: emptyList()
            val studentsInStation = allStudents
                .filter { assignedAlbumNumbers.contains(it.albumNumber) }
                .sortedBy { it.surname } // sortowanie po nazwiskach od lewej do prawej

            if (studentsInStation.isNotEmpty()) {
                // Obliczenie wysokości kolumny dla każdego studentan
                var maxStudentBlockHeight = 0
                studentsInStation.forEach { student ->
                    val studentPassedTasks = passedTasks[student.albumNumber] ?: emptySet()
                    val passedTasksWithGrades = allTasks
                        .filter { task -> studentPassedTasks.contains(task.description) }
                        .sortedBy { it.grade }
                    // 3 wersy dane studenta (name, surname, album) + 1 for proposed grade + 1 for task header + liczba zadań (min 1 if no tasks)
                    maxStudentBlockHeight = maxOf(maxStudentBlockHeight, 5 + maxOf(1, passedTasksWithGrades.size))
                }

                var currentStudentCol = 0 // Starting column for the first student in this station

                studentsInStation.forEach { student ->
                    // Calculate studentStartRow relative to the station's content start
                    val studentStartRow = currentStationContentStartRow // Each student starts at the top of their block

                    //  (Imię, Nazwisko, Numer Albumu)
                    val rowName = sheet.getRow(studentStartRow) ?: sheet.createRow(studentStartRow)
                    rowName.createCell(currentStudentCol).setCellValue("Imię: ${student.name}")

                    val rowSurname = sheet.getRow(studentStartRow + 1) ?: sheet.createRow(studentStartRow + 1)
                    rowSurname.createCell(currentStudentCol).setCellValue("Nazwisko: ${student.surname}")

                    val rowAlbum = sheet.getRow(studentStartRow + 2) ?: sheet.createRow(studentStartRow + 2)
                    rowAlbum.createCell(currentStudentCol).setCellValue("Album: ${student.albumNumber}")

                    // Proposed Grade row
                    val rowProposedGrade = sheet.getRow(studentStartRow + 3) ?: sheet.createRow(studentStartRow + 3)
                    rowProposedGrade.createCell(currentStudentCol).setCellValue("Proponowana Ocena: ${student.proposedGrade}")



                    val rowTaskHeader = sheet.getRow(studentStartRow + 4) ?: sheet.createRow(studentStartRow + 4)
                    rowTaskHeader.createCell(currentStudentCol).setCellValue("Zaliczone Zadanie")
                    rowTaskHeader.createCell(currentStudentCol + 1).setCellValue("Ocena")

                    var currentTaskWriteRow = studentStartRow + 5 // Row to start writing tasks for current student

                    // --- Zadania zdane---
                    val studentTasksPassed = passedTasks[student.albumNumber] ?: emptySet()
                    val passedTasksWithGrades = allTasks
                        .filter { task -> studentTasksPassed.contains(task.description) }
                        .sortedBy { it.grade }

                    if (passedTasksWithGrades.isEmpty()) {
                        val rowNoTasks = sheet.getRow(currentTaskWriteRow) ?: sheet.createRow(currentTaskWriteRow)
                        rowNoTasks.createCell(currentStudentCol).setCellValue("Brak zaliczonych zadań")
                        rowNoTasks.createCell(currentStudentCol + 1).setCellValue("") // Empty grade

                    } else {
                        passedTasksWithGrades.forEach { task ->
                            val rowTask = sheet.getRow(currentTaskWriteRow) ?: sheet.createRow(currentTaskWriteRow)
                            rowTask.createCell(currentStudentCol).setCellValue(task.description)
                            rowTask.createCell(currentStudentCol + 1).setCellValue(task.grade)

                            currentTaskWriteRow++
                        }
                    }
                    currentStudentCol += 3 // Move to the columns for the next student block (3 columns wide per student: details, task description, task grade)
                }
                // After processing all students in the station, advance currentRowIndex for the next station.
                // It should be the starting row of the station content + the maximum block height any student took.
                currentRowIndex = currentStationContentStartRow + maxStudentBlockHeight


            } else {
                val row = sheet.createRow(currentRowIndex++)
                row.createCell(0).setCellValue("Brak studentów na tej stacji")

            }
            currentRowIndex++ // one blank row between stations for better separation

        }



        workbook.write(outputStream)
        workbook.close()
    }
}