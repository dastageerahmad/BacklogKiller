package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffects
import com.example.data.KaizenRepository
import com.example.model.AppSettings
import com.example.model.Flashcard
import com.example.model.Lecture
import com.example.model.LectureFlags
import com.example.model.LectureLinks
import com.example.model.LibraryItem
import com.example.model.MistakeItem
import com.example.model.MockTest
import com.example.model.StudyLogDay
import com.example.model.Subject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class KaizenTab {
    HOME, BACKLOG, PLANNER, TIMER, MENU, SETTINGS
}

enum class MenuSubScreen {
    REVISE, MISTAKES, FLASHCARDS, MOCK_TESTS, LIBRARY, INSIGHTS, WEEKLY_REPORT, STREAK_HEATMAP
}

data class KaizenUiState(
    val subjects: List<Subject> = emptyList(),
    val lectures: List<Lecture> = emptyList(),
    val studyLogs: List<StudyLogDay> = emptyList(),
    val libraryItems: List<LibraryItem> = emptyList(),
    val mistakes: List<MistakeItem> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),
    val mockTests: List<MockTest> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val currentTab: KaizenTab = KaizenTab.HOME,
    val menuSubScreen: MenuSubScreen? = null,
    val showProfileDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val celebrationTimestamp: Long = 0L,
    val canUndo: Boolean = false,

    // Backlog filters
    val backlogSubjectFilter: String? = null,
    val backlogStatusFilter: String = "ALL", // ALL, PENDING, IN_PROGRESS, DONE
    val backlogSearchQuery: String = "",

    // Timer
    val timerMode: String = "COUNTDOWN", // COUNTDOWN or STOPWATCH
    val timerPresetMinutes: Int = 50,
    val timerRemainingSeconds: Int = 50 * 60,
    val timerElapsedSeconds: Int = 0,
    val timerIsRunning: Boolean = false,
    val timerSubjectId: String = "sub_phy",
    val timerKeepScreenAwake: Boolean = true,
    val timerAmbientSound: String = "none"
)

class KaizenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = KaizenRepository(application)
    private val _uiState = MutableStateFlow(KaizenUiState())
    val uiState: StateFlow<KaizenUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadAllData()
    }

    fun loadAllData() {
        val subs = repository.getSubjects()
        val lecs = repository.getLectures()
        val logs = repository.getStudyLogs()
        val lib = repository.getLibraryItems()
        val mis = repository.getMistakes()
        val flash = repository.getFlashcards()
        val mocks = repository.getMockTests()
        val sett = repository.getSettings()

        _uiState.update {
            it.copy(
                subjects = subs,
                lectures = lecs,
                studyLogs = logs,
                libraryItems = lib,
                mistakes = mis,
                flashcards = flash,
                mockTests = mocks,
                settings = sett,
                timerSubjectId = subs.firstOrNull()?.id ?: "sub_phy"
            )
        }
    }

    fun navigateToTab(tab: KaizenTab) {
        _uiState.update { it.copy(currentTab = tab, menuSubScreen = null) }
    }

    fun navigateToMenuSubScreen(subScreen: MenuSubScreen?) {
        _uiState.update { it.copy(menuSubScreen = subScreen) }
    }

    fun setProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showProfileDialog = show) }
    }

    fun setExitDialog(show: Boolean) {
        _uiState.update { it.copy(showExitDialog = show) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    // --- LECTURE OPERATIONS ---

    fun toggleLectureStatus(lectureId: String) {
        val now = System.currentTimeMillis()
        var markedDone = false
        val updated = _uiState.value.lectures.map { lec ->
            if (lec.id == lectureId) {
                if (lec.status == "done") {
                    lec.copy(status = "pending", completedAt = null)
                } else {
                    markedDone = true
                    lec.copy(status = "done", completedAt = now)
                }
            } else {
                lec
            }
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)

        if (markedDone) {
            val settings = _uiState.value.settings
            if (settings.soundEnabled) {
                SoundEffects.playCompletionPop(settings.soundStyle, settings.soundPitch, settings.soundVolume)
            }
            _uiState.update {
                it.copy(
                    celebrationTimestamp = now,
                    snackbarMessage = "Lecture completed! +1 towards NEET target"
                )
            }
        }
    }

    fun toggleTodayPick(lectureId: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updated = _uiState.value.lectures.map { lec ->
            if (lec.id == lectureId) {
                val newToday = if (lec.todayDate == todayStr) null else todayStr
                lec.copy(todayDate = newToday)
            } else {
                lec
            }
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
    }

    fun addLecture(
        subjectId: String,
        chapter: String,
        title: String,
        durationMinutes: Int,
        speed: Float,
        priority: String
    ) {
        val newLec = Lecture(
            id = UUID.randomUUID().toString(),
            subjectId = subjectId,
            chapter = chapter.trim(),
            title = title.trim(),
            durationMinutes = durationMinutes,
            speed = speed,
            priority = priority
        )
        val list = _uiState.value.lectures + newLec
        _uiState.update { it.copy(lectures = list) }
        repository.saveLectures(list)
        showMessage("Added lecture '${title.take(20)}...'")
    }

    fun updateLecture(lecture: Lecture) {
        val updated = _uiState.value.lectures.map { if (it.id == lecture.id) lecture else it }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
        showMessage("Lecture updated")
    }

    fun deleteLecture(lectureId: String) {
        val found = _uiState.value.lectures.find { it.id == lectureId } ?: return
        repository.deletedLectureStack.add(found)
        val updated = _uiState.value.lectures.filterNot { it.id == lectureId }
        _uiState.update { it.copy(lectures = updated, canUndo = true) }
        repository.saveLectures(updated)
        showMessage("Deleted lecture '${found.title.take(18)}'")
    }

    fun deleteChapter(subjectId: String, chapterName: String) {
        val toDelete = _uiState.value.lectures.filter { it.subjectId == subjectId && it.chapter == chapterName }
        repository.deletedLectureStack.addAll(toDelete)
        val updated = _uiState.value.lectures.filterNot { it.subjectId == subjectId && it.chapter == chapterName }
        _uiState.update { it.copy(lectures = updated, canUndo = toDelete.isNotEmpty()) }
        repository.saveLectures(updated)
        showMessage("Deleted chapter '$chapterName' (${toDelete.size} lectures)")
    }

    fun undoLastDeletion() {
        if (repository.deletedLectureStack.isNotEmpty()) {
            val restored = repository.deletedLectureStack.removeAt(repository.deletedLectureStack.lastIndex)
            val updated = _uiState.value.lectures + restored
            _uiState.update {
                it.copy(
                    lectures = updated,
                    canUndo = repository.deletedLectureStack.isNotEmpty()
                )
            }
            repository.saveLectures(updated)
            showMessage("Restored '${restored.title.take(20)}'")
        }
    }

    fun updateLectureChip(lectureId: String, chipType: String, linkUrl: String?, toggleDone: Boolean) {
        val updated = _uiState.value.lectures.map { lec ->
            if (lec.id == lectureId) {
                var flags = lec.flags
                var links = lec.links
                when (chipType.uppercase()) {
                    "DPP" -> {
                        if (linkUrl != null) links = links.copy(dpp = linkUrl)
                        if (toggleDone) flags = flags.copy(dpp = !flags.dpp)
                    }
                    "NOTES" -> {
                        if (linkUrl != null) links = links.copy(notes = linkUrl)
                        if (toggleDone) flags = flags.copy(notes = !flags.notes)
                    }
                    "HW" -> {
                        if (linkUrl != null) links = links.copy(hw = linkUrl)
                        if (toggleDone) flags = flags.copy(hw = !flags.hw)
                    }
                    "EXTRAQS" -> {
                        if (linkUrl != null) links = links.copy(extraQs = linkUrl)
                        if (toggleDone) flags = flags.copy(extraQs = !flags.extraQs)
                    }
                }
                lec.copy(flags = flags, links = links)
            } else {
                lec
            }
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
    }

    // --- SUBJECT OPERATIONS ---
    fun addSubject(name: String, colorHex: String) {
        val newSub = Subject(
            id = "sub_" + UUID.randomUUID().toString().take(6),
            name = name.trim(),
            colorHex = colorHex
        )
        val updated = _uiState.value.subjects + newSub
        _uiState.update { it.copy(subjects = updated) }
        repository.saveSubjects(updated)
        showMessage("Subject '$name' created")
    }

    fun updateSubject(subject: Subject) {
        val updated = _uiState.value.subjects.map { if (it.id == subject.id) subject else it }
        _uiState.update { it.copy(subjects = updated) }
        repository.saveSubjects(updated)
    }

    fun deleteSubject(subjectId: String) {
        val sub = _uiState.value.subjects.find { it.id == subjectId } ?: return
        repository.deletedSubjectStack.add(sub)
        val updatedSubs = _uiState.value.subjects.filterNot { it.id == subjectId }
        val updatedLecs = _uiState.value.lectures.filterNot { it.subjectId == subjectId }
        _uiState.update { it.copy(subjects = updatedSubs, lectures = updatedLecs) }
        repository.saveSubjects(updatedSubs)
        repository.saveLectures(updatedLecs)
        showMessage("Subject '${sub.name}' deleted")
    }

    // --- BACKLOG FILTER ACTIONS ---
    fun setBacklogSubjectFilter(subId: String?) {
        _uiState.update { it.copy(backlogSubjectFilter = subId) }
    }

    fun setBacklogStatusFilter(status: String) {
        _uiState.update { it.copy(backlogStatusFilter = status) }
    }

    fun setBacklogSearchQuery(query: String) {
        _uiState.update { it.copy(backlogSearchQuery = query) }
    }

    // --- PLANNER ACTIONS ---
    fun moveLectureToDate(lectureId: String, newDate: String?) {
        val updated = _uiState.value.lectures.map {
            if (it.id == lectureId) it.copy(plannedDate = newDate) else it
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
        showMessage("Planner updated")
    }

    fun autoScheduleBacklog() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val dailyGoalMinutes = (_uiState.value.settings.dailyStudyGoalHours * 60).toInt().coerceAtLeast(60)

        // Spread pending lectures
        val pending = _uiState.value.lectures.filter { it.status != "done" }
        var currentDayMins = 0
        val updatedMap = mutableMapOf<String, String>()

        for (lec in pending) {
            val dur = lec.effectiveDurationMinutes
            if (currentDayMins + dur > dailyGoalMinutes && currentDayMins > 0) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                currentDayMins = 0
            }
            updatedMap[lec.id] = sdf.format(cal.time)
            currentDayMins += dur
        }

        val updated = _uiState.value.lectures.map { lec ->
            if (updatedMap.containsKey(lec.id)) {
                lec.copy(plannedDate = updatedMap[lec.id])
            } else {
                lec
            }
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
        showMessage("Auto-scheduled ${pending.size} lectures across future days!")
    }

    // --- TIMER ACTIONS ---
    fun setTimerMode(mode: String) {
        stopTimer()
        _uiState.update {
            it.copy(
                timerMode = mode,
                timerRemainingSeconds = if (mode == "COUNTDOWN") it.timerPresetMinutes * 60 else 0,
                timerElapsedSeconds = 0
            )
        }
    }

    fun setTimerPreset(minutes: Int) {
        stopTimer()
        _uiState.update {
            it.copy(
                timerPresetMinutes = minutes,
                timerRemainingSeconds = minutes * 60,
                timerElapsedSeconds = 0
            )
        }
    }

    fun setTimerSubject(subjectId: String) {
        _uiState.update { it.copy(timerSubjectId = subjectId) }
    }

    fun toggleKeepScreenAwake() {
        _uiState.update { it.copy(timerKeepScreenAwake = !it.timerKeepScreenAwake) }
    }

    fun setAmbientSound(ambient: String) {
        _uiState.update { it.copy(timerAmbientSound = ambient) }
        if (ambient == "none") {
            SoundEffects.stopAmbient()
        } else {
            SoundEffects.startAmbient(ambient, _uiState.value.settings.soundVolume)
        }
    }

    fun startTimer() {
        if (_uiState.value.timerIsRunning) return
        _uiState.update { it.copy(timerIsRunning = true) }

        if (_uiState.value.timerAmbientSound != "none") {
            SoundEffects.startAmbient(_uiState.value.timerAmbientSound, _uiState.value.settings.soundVolume)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _uiState.value
                if (!state.timerIsRunning) break

                if (state.timerMode == "COUNTDOWN") {
                    if (state.timerRemainingSeconds > 1) {
                        _uiState.update { it.copy(timerRemainingSeconds = it.timerRemainingSeconds - 1) }
                    } else {
                        // Finished!
                        _uiState.update {
                            it.copy(
                                timerRemainingSeconds = 0,
                                timerIsRunning = false
                            )
                        }
                        SoundEffects.stopAmbient()
                        if (state.settings.soundEnabled) {
                            SoundEffects.playCompletionPop(state.settings.soundStyle, state.settings.soundPitch, state.settings.soundVolume)
                        }
                        showMessage("Study session completed! Great focus.")
                        break
                    }
                } else {
                    _uiState.update { it.copy(timerElapsedSeconds = it.timerElapsedSeconds + 1) }
                }
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(timerIsRunning = false) }
        timerJob?.cancel()
        timerJob = null
        SoundEffects.stopAmbient()
    }

    fun resetTimer() {
        stopTimer()
        _uiState.update {
            it.copy(
                timerRemainingSeconds = it.timerPresetMinutes * 60,
                timerElapsedSeconds = 0
            )
        }
    }

    private fun stopTimer() {
        _uiState.update { it.copy(timerIsRunning = false) }
        timerJob?.cancel()
        timerJob = null
        SoundEffects.stopAmbient()
    }

    fun saveTimerSessionToLog() {
        val state = _uiState.value
        val minutes = if (state.timerMode == "COUNTDOWN") {
            val studied = (state.timerPresetMinutes * 60 - state.timerRemainingSeconds) / 60
            studied.coerceAtLeast(1)
        } else {
            (state.timerElapsedSeconds / 60).coerceAtLeast(1)
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val subjectId = state.timerSubjectId
        val existingLogs = state.studyLogs.toMutableList()
        val index = existingLogs.indexOfFirst { it.date == todayStr }

        if (index >= 0) {
            val curr = existingLogs[index]
            val currSub = curr.subjectMinutes[subjectId] ?: 0
            val newSubMap = curr.subjectMinutes.toMutableMap()
            newSubMap[subjectId] = currSub + minutes
            existingLogs[index] = curr.copy(
                totalMinutes = curr.totalMinutes + minutes,
                subjectMinutes = newSubMap
            )
        } else {
            existingLogs.add(
                StudyLogDay(
                    date = todayStr,
                    totalMinutes = minutes,
                    subjectMinutes = mapOf(subjectId to minutes)
                )
            )
        }

        _uiState.update { it.copy(studyLogs = existingLogs) }
        repository.saveStudyLogs(existingLogs)
        showMessage("Saved $minutes mins study session to log!")
        resetTimer()
    }

    // --- REVISION ACTIONS ---
    fun toggleRevision(lectureId: String, dayKey: Int) {
        val updated = _uiState.value.lectures.map { lec ->
            if (lec.id == lectureId) {
                when (dayKey) {
                    1 -> lec.copy(revisionDay1 = !lec.revisionDay1)
                    3 -> lec.copy(revisionDay3 = !lec.revisionDay3)
                    7 -> lec.copy(revisionDay7 = !lec.revisionDay7)
                    21 -> lec.copy(revisionDay21 = !lec.revisionDay21)
                    else -> lec
                }
            } else {
                lec
            }
        }
        _uiState.update { it.copy(lectures = updated) }
        repository.saveLectures(updated)
        showMessage("Revision status updated")
    }

    // --- MISTAKES ACTIONS ---
    fun addMistake(subjectId: String, title: String, type: String, notes: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val item = MistakeItem(
            id = UUID.randomUUID().toString(),
            subjectId = subjectId,
            questionTitle = title.trim(),
            mistakeType = type,
            notes = notes.trim(),
            solvedCount = 0,
            date = todayStr
        )
        val updated = _uiState.value.mistakes + item
        _uiState.update { it.copy(mistakes = updated) }
        repository.saveMistakes(updated)
        showMessage("Logged mistake for review")
    }

    fun incrementMistakeSolved(mistakeId: String) {
        val updated = _uiState.value.mistakes.map {
            if (it.id == mistakeId) it.copy(solvedCount = it.solvedCount + 1) else it
        }
        _uiState.update { it.copy(mistakes = updated) }
        repository.saveMistakes(updated)
    }

    fun deleteMistake(mistakeId: String) {
        val updated = _uiState.value.mistakes.filterNot { it.id == mistakeId }
        _uiState.update { it.copy(mistakes = updated) }
        repository.saveMistakes(updated)
        showMessage("Mistake removed")
    }

    // --- FLASHCARD ACTIONS ---
    fun addFlashcard(subjectId: String, chapter: String, front: String, back: String) {
        val card = Flashcard(
            id = UUID.randomUUID().toString(),
            subjectId = subjectId,
            chapter = chapter.trim(),
            front = front.trim(),
            back = back.trim(),
            mastered = false
        )
        val updated = _uiState.value.flashcards + card
        _uiState.update { it.copy(flashcards = updated) }
        repository.saveFlashcards(updated)
        showMessage("Flashcard created")
    }

    fun toggleFlashcardMastered(cardId: String) {
        val updated = _uiState.value.flashcards.map {
            if (it.id == cardId) it.copy(mastered = !it.mastered) else it
        }
        _uiState.update { it.copy(flashcards = updated) }
        repository.saveFlashcards(updated)
    }

    fun deleteFlashcard(cardId: String) {
        val updated = _uiState.value.flashcards.filterNot { it.id == cardId }
        _uiState.update { it.copy(flashcards = updated) }
        repository.saveFlashcards(updated)
        showMessage("Flashcard deleted")
    }

    // --- MOCK TEST ACTIONS ---
    fun addMockTest(testName: String, date: String, phy: Int, chem: Int, bio: Int, notes: String) {
        val mt = MockTest(
            id = UUID.randomUUID().toString(),
            testName = testName.trim(),
            date = date,
            physicsScore = phy,
            chemistryScore = chem,
            biologyScore = bio,
            analysisNotes = notes.trim()
        )
        val updated = _uiState.value.mockTests + mt
        _uiState.update { it.copy(mockTests = updated) }
        repository.saveMockTests(updated)
        showMessage("Mock test score (${mt.totalScore}/720) logged!")
    }

    fun deleteMockTest(testId: String) {
        val updated = _uiState.value.mockTests.filterNot { it.id == testId }
        _uiState.update { it.copy(mockTests = updated) }
        repository.saveMockTests(updated)
        showMessage("Mock test removed")
    }

    // --- LIBRARY ACTIONS ---
    fun addLibraryItem(title: String, linkOrNote: String, subjectId: String, type: String, chapter: String? = null) {
        val item = LibraryItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            linkOrNote = linkOrNote.trim(),
            subjectId = subjectId,
            type = type,
            done = false,
            chapter = chapter
        )
        val updated = _uiState.value.libraryItems + item
        _uiState.update { it.copy(libraryItems = updated) }
        repository.saveLibraryItems(updated)
        showMessage("Resource added to library")
    }

    fun toggleLibraryDone(itemId: String) {
        val updated = _uiState.value.libraryItems.map {
            if (it.id == itemId) it.copy(done = !it.done) else it
        }
        _uiState.update { it.copy(libraryItems = updated) }
        repository.saveLibraryItems(updated)
    }

    fun deleteLibraryItem(itemId: String) {
        val updated = _uiState.value.libraryItems.filterNot { it.id == itemId }
        _uiState.update { it.copy(libraryItems = updated) }
        repository.saveLibraryItems(updated)
        showMessage("Resource deleted")
    }

    fun bulkAddLibrary(subjectId: String, type: String, chapter: String?, rawLines: String) {
        val lines = rawLines.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val newItems = mutableListOf<LibraryItem>()
        var count = 0

        for (line in lines) {
            val parts = line.split("\\s+".toRegex(), limit = 2)
            var lecNum: Int? = null
            var url = line
            var title = "$type Item ${count + 1}"

            if (parts.size == 2 && parts[0].toIntOrNull() != null) {
                lecNum = parts[0].toInt()
                url = parts[1]
                title = "$type $lecNum"
            } else if (line.startsWith("http")) {
                title = "$type ${(count + 1)}"
            }

            newItems.add(
                LibraryItem(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    linkOrNote = url,
                    subjectId = subjectId,
                    type = type,
                    done = false,
                    lectureNumber = lecNum,
                    chapter = chapter
                )
            )
            count++
        }

        val updated = _uiState.value.libraryItems + newItems
        _uiState.update { it.copy(libraryItems = updated) }
        repository.saveLibraryItems(updated)
        showMessage("Bulk added $count resources to Library!")
    }

    // --- SETTINGS & PROFILE ACTIONS ---
    fun updateSettings(newSettings: AppSettings) {
        _uiState.update { it.copy(settings = newSettings) }
        repository.saveSettings(newSettings)
    }

    fun resetAllData() {
        stopTimer()
        repository.resetAllData()
        loadAllData()
        showMessage("All data reset to initial default.")
    }

    fun exportBackup(): String {
        return repository.exportBackupJson()
    }

    fun importBackup(json: String): Boolean {
        val ok = repository.importBackupJson(json)
        if (ok) {
            loadAllData()
            showMessage("Data restored successfully!")
        } else {
            showMessage("Invalid backup JSON format.")
        }
        return ok
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
