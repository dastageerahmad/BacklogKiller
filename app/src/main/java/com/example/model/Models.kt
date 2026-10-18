package com.example.model

data class Subject(
    val id: String,
    val name: String,
    val colorHex: String
)

data class LectureFlags(
    val dpp: Boolean = false,
    val notes: Boolean = false,
    val hw: Boolean = false,
    val extraQs: Boolean = false
)

data class LectureLinks(
    val dpp: String? = null,
    val notes: String? = null,
    val hw: String? = null,
    val extraQs: String? = null
)

data class Lecture(
    val id: String,
    val subjectId: String,
    val chapter: String,
    val title: String,
    val durationMinutes: Int,
    val speed: Float = 1.0f,
    val status: String = "pending", // "pending", "in_progress", "done"
    val flags: LectureFlags = LectureFlags(),
    val links: LectureLinks = LectureLinks(),
    val todayDate: String? = null,
    val priority: String = "medium", // "high", "medium", "low"
    val completedAt: Long? = null,
    val plannedDate: String? = null,
    val revisionDay1: Boolean = false,
    val revisionDay3: Boolean = false,
    val revisionDay7: Boolean = false,
    val revisionDay21: Boolean = false
) {
    val effectiveDurationMinutes: Int
        get() = if (speed > 0f) (durationMinutes / speed).toInt().coerceAtLeast(1) else durationMinutes
}

data class StudyLogDay(
    val date: String, // "YYYY-MM-DD"
    val totalMinutes: Int,
    val subjectMinutes: Map<String, Int> = emptyMap()
)

data class LibraryItem(
    val id: String,
    val title: String,
    val linkOrNote: String,
    val subjectId: String,
    val type: String, // "DPP", "Class notes", "HW", "Extra Qs"
    val done: Boolean = false,
    val lectureNumber: Int? = null,
    val chapter: String? = null
)

data class MistakeItem(
    val id: String,
    val subjectId: String,
    val questionTitle: String,
    val mistakeType: String, // "Calculation error", "Concept gap", "Misread question", "Formula forgotten", "Time pressure"
    val notes: String,
    val solvedCount: Int = 0,
    val date: String
)

data class Flashcard(
    val id: String,
    val subjectId: String,
    val chapter: String,
    val front: String,
    val back: String,
    val mastered: Boolean = false
)

data class MockTest(
    val id: String,
    val testName: String,
    val date: String,
    val physicsScore: Int,
    val chemistryScore: Int,
    val biologyScore: Int,
    val analysisNotes: String = ""
) {
    val totalScore: Int get() = (physicsScore + chemistryScore + biologyScore).coerceIn(0, 720)
    val percentage: Float get() = (totalScore.toFloat() / 720f) * 100f
}

data class AppSettings(
    val examName: String = "NEET UG 2028",
    val examDate: String = "2028-05-07",
    val dailyStudyGoalHours: Float = 6.0f,
    val userName: String = "Aryan",
    val themeMode: String = "matte_dark", // "dark_navy", "matte_dark", "matte_pastel", "light"
    val grainEnabled: Boolean = false,
    val simpleMode: Boolean = false,
    val accentColor: String = "#14B8A6", // Teal default
    val buttonScale: Float = 1.0f,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val soundStyle: String = "Classic", // "Classic", "Soft", "Retro", "Bell"
    val soundPitch: Float = 1.0f
)
