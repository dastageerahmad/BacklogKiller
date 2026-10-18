package com.example.data

import android.content.Context
import android.content.SharedPreferences
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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class KaizenRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kaizen_neet_prefs", Context.MODE_PRIVATE)

    var deletedLectureStack: MutableList<Lecture> = mutableListOf()
    var deletedSubjectStack: MutableList<Subject> = mutableListOf()

    init {
        if (!prefs.getBoolean("seeded_v1", false)) {
            seedInitialData()
            prefs.edit().putBoolean("seeded_v1", true).apply()
        }
    }

    // --- SUBJECTS ---
    fun getSubjects(): List<Subject> {
        val json = prefs.getString("subjects", null) ?: return defaultSubjects()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Subject>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(Subject(o.getString("id"), o.getString("name"), o.getString("colorHex")))
            }
            if (list.isEmpty()) defaultSubjects() else list
        } catch (_: Exception) {
            defaultSubjects()
        }
    }

    fun saveSubjects(list: List<Subject>) {
        val arr = JSONArray()
        for (s in list) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("name", s.name)
            o.put("colorHex", s.colorHex)
            arr.put(o)
        }
        prefs.edit().putString("subjects", arr.toString()).apply()
    }

    // --- LECTURES ---
    fun getLectures(): List<Lecture> {
        val json = prefs.getString("lectures", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Lecture>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val flagsObj = o.optJSONObject("flags") ?: JSONObject()
                val linksObj = o.optJSONObject("links") ?: JSONObject()

                val flags = LectureFlags(
                    dpp = flagsObj.optBoolean("dpp", false),
                    notes = flagsObj.optBoolean("notes", false),
                    hw = flagsObj.optBoolean("hw", false),
                    extraQs = flagsObj.optBoolean("extraQs", false)
                )

                val links = LectureLinks(
                    dpp = if (linksObj.has("dpp") && !linksObj.isNull("dpp")) linksObj.getString("dpp") else null,
                    notes = if (linksObj.has("notes") && !linksObj.isNull("notes")) linksObj.getString("notes") else null,
                    hw = if (linksObj.has("hw") && !linksObj.isNull("hw")) linksObj.getString("hw") else null,
                    extraQs = if (linksObj.has("extraQs") && !linksObj.isNull("extraQs")) linksObj.getString("extraQs") else null
                )

                list.add(
                    Lecture(
                        id = o.getString("id"),
                        subjectId = o.getString("subjectId"),
                        chapter = o.getString("chapter"),
                        title = o.getString("title"),
                        durationMinutes = o.getInt("durationMinutes"),
                        speed = o.optDouble("speed", 1.0).toFloat(),
                        status = o.optString("status", "pending"),
                        flags = flags,
                        links = links,
                        todayDate = if (o.has("todayDate") && !o.isNull("todayDate")) o.getString("todayDate") else null,
                        priority = o.optString("priority", "medium"),
                        completedAt = if (o.has("completedAt") && !o.isNull("completedAt")) o.getLong("completedAt") else null,
                        plannedDate = if (o.has("plannedDate") && !o.isNull("plannedDate")) o.getString("plannedDate") else null,
                        revisionDay1 = o.optBoolean("revisionDay1", false),
                        revisionDay3 = o.optBoolean("revisionDay3", false),
                        revisionDay7 = o.optBoolean("revisionDay7", false),
                        revisionDay21 = o.optBoolean("revisionDay21", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveLectures(list: List<Lecture>) {
        val arr = JSONArray()
        for (l in list) {
            val o = JSONObject()
            o.put("id", l.id)
            o.put("subjectId", l.subjectId)
            o.put("chapter", l.chapter)
            o.put("title", l.title)
            o.put("durationMinutes", l.durationMinutes)
            o.put("speed", l.speed.toDouble())
            o.put("status", l.status)

            val f = JSONObject()
            f.put("dpp", l.flags.dpp)
            f.put("notes", l.flags.notes)
            f.put("hw", l.flags.hw)
            f.put("extraQs", l.flags.extraQs)
            o.put("flags", f)

            val lk = JSONObject()
            l.links.dpp?.let { lk.put("dpp", it) }
            l.links.notes?.let { lk.put("notes", it) }
            l.links.hw?.let { lk.put("hw", it) }
            l.links.extraQs?.let { lk.put("extraQs", it) }
            o.put("links", lk)

            l.todayDate?.let { o.put("todayDate", it) }
            o.put("priority", l.priority)
            l.completedAt?.let { o.put("completedAt", it) }
            l.plannedDate?.let { o.put("plannedDate", it) }
            o.put("revisionDay1", l.revisionDay1)
            o.put("revisionDay3", l.revisionDay3)
            o.put("revisionDay7", l.revisionDay7)
            o.put("revisionDay21", l.revisionDay21)

            arr.put(o)
        }
        prefs.edit().putString("lectures", arr.toString()).apply()
    }

    // --- STUDY LOGS ---
    fun getStudyLogs(): List<StudyLogDay> {
        val json = prefs.getString("study_logs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<StudyLogDay>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val smObj = o.optJSONObject("subjectMinutes") ?: JSONObject()
                val sm = mutableMapOf<String, Int>()
                val keys = smObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    sm[k] = smObj.getInt(k)
                }
                list.add(StudyLogDay(o.getString("date"), o.getInt("totalMinutes"), sm))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveStudyLogs(list: List<StudyLogDay>) {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("date", item.date)
            o.put("totalMinutes", item.totalMinutes)
            val sm = JSONObject()
            for ((k, v) in item.subjectMinutes) {
                sm.put(k, v)
            }
            o.put("subjectMinutes", sm)
            arr.put(o)
        }
        prefs.edit().putString("study_logs", arr.toString()).apply()
    }

    // --- LIBRARY ITEMS ---
    fun getLibraryItems(): List<LibraryItem> {
        val json = prefs.getString("library_items", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<LibraryItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    LibraryItem(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        linkOrNote = o.getString("linkOrNote"),
                        subjectId = o.getString("subjectId"),
                        type = o.getString("type"),
                        done = o.optBoolean("done", false),
                        lectureNumber = if (o.has("lectureNumber")) o.getInt("lectureNumber") else null,
                        chapter = if (o.has("chapter")) o.getString("chapter") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveLibraryItems(list: List<LibraryItem>) {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("id", item.id)
            o.put("title", item.title)
            o.put("linkOrNote", item.linkOrNote)
            o.put("subjectId", item.subjectId)
            o.put("type", item.type)
            o.put("done", item.done)
            item.lectureNumber?.let { o.put("lectureNumber", it) }
            item.chapter?.let { o.put("chapter", it) }
            arr.put(o)
        }
        prefs.edit().putString("library_items", arr.toString()).apply()
    }

    // --- MISTAKES ---
    fun getMistakes(): List<MistakeItem> {
        val json = prefs.getString("mistakes", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<MistakeItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    MistakeItem(
                        id = o.getString("id"),
                        subjectId = o.getString("subjectId"),
                        questionTitle = o.getString("questionTitle"),
                        mistakeType = o.getString("mistakeType"),
                        notes = o.optString("notes", ""),
                        solvedCount = o.optInt("solvedCount", 0),
                        date = o.getString("date")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMistakes(list: List<MistakeItem>) {
        val arr = JSONArray()
        for (m in list) {
            val o = JSONObject()
            o.put("id", m.id)
            o.put("subjectId", m.subjectId)
            o.put("questionTitle", m.questionTitle)
            o.put("mistakeType", m.mistakeType)
            o.put("notes", m.notes)
            o.put("solvedCount", m.solvedCount)
            o.put("date", m.date)
            arr.put(o)
        }
        prefs.edit().putString("mistakes", arr.toString()).apply()
    }

    // --- FLASHCARDS ---
    fun getFlashcards(): List<Flashcard> {
        val json = prefs.getString("flashcards", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Flashcard>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Flashcard(
                        id = o.getString("id"),
                        subjectId = o.getString("subjectId"),
                        chapter = o.getString("chapter"),
                        front = o.getString("front"),
                        back = o.getString("back"),
                        mastered = o.optBoolean("mastered", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveFlashcards(list: List<Flashcard>) {
        val arr = JSONArray()
        for (f in list) {
            val o = JSONObject()
            o.put("id", f.id)
            o.put("subjectId", f.subjectId)
            o.put("chapter", f.chapter)
            o.put("front", f.front)
            o.put("back", f.back)
            o.put("mastered", f.mastered)
            arr.put(o)
        }
        prefs.edit().putString("flashcards", arr.toString()).apply()
    }

    // --- MOCK TESTS ---
    fun getMockTests(): List<MockTest> {
        val json = prefs.getString("mock_tests", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<MockTest>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    MockTest(
                        id = o.getString("id"),
                        testName = o.getString("testName"),
                        date = o.getString("date"),
                        physicsScore = o.getInt("physicsScore"),
                        chemistryScore = o.getInt("chemistryScore"),
                        biologyScore = o.getInt("biologyScore"),
                        analysisNotes = o.optString("analysisNotes", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMockTests(list: List<MockTest>) {
        val arr = JSONArray()
        for (mt in list) {
            val o = JSONObject()
            o.put("id", mt.id)
            o.put("testName", mt.testName)
            o.put("date", mt.date)
            o.put("physicsScore", mt.physicsScore)
            o.put("chemistryScore", mt.chemistryScore)
            o.put("biologyScore", mt.biologyScore)
            o.put("analysisNotes", mt.analysisNotes)
            arr.put(o)
        }
        prefs.edit().putString("mock_tests", arr.toString()).apply()
    }

    // --- SETTINGS ---
    fun getSettings(): AppSettings {
        val json = prefs.getString("settings", null) ?: return AppSettings()
        return try {
            val o = JSONObject(json)
            AppSettings(
                examName = o.optString("examName", "NEET UG 2028"),
                examDate = o.optString("examDate", "2028-05-07"),
                dailyStudyGoalHours = o.optDouble("dailyStudyGoalHours", 6.0).toFloat(),
                userName = o.optString("userName", "Aryan"),
                themeMode = o.optString("themeMode", "matte_dark"),
                grainEnabled = o.optBoolean("grainEnabled", false),
                simpleMode = o.optBoolean("simpleMode", false),
                accentColor = o.optString("accentColor", "#14B8A6"),
                buttonScale = o.optDouble("buttonScale", 1.0).toFloat(),
                soundEnabled = o.optBoolean("soundEnabled", true),
                soundVolume = o.optDouble("soundVolume", 0.8).toFloat(),
                soundStyle = o.optString("soundStyle", "Classic"),
                soundPitch = o.optDouble("soundPitch", 1.0).toFloat()
            )
        } catch (_: Exception) {
            AppSettings()
        }
    }

    fun saveSettings(s: AppSettings) {
        val o = JSONObject()
        o.put("examName", s.examName)
        o.put("examDate", s.examDate)
        o.put("dailyStudyGoalHours", s.dailyStudyGoalHours.toDouble())
        o.put("userName", s.userName)
        o.put("themeMode", s.themeMode)
        o.put("grainEnabled", s.grainEnabled)
        o.put("simpleMode", s.simpleMode)
        o.put("accentColor", s.accentColor)
        o.put("buttonScale", s.buttonScale.toDouble())
        o.put("soundEnabled", s.soundEnabled)
        o.put("soundVolume", s.soundVolume.toDouble())
        o.put("soundStyle", s.soundStyle)
        o.put("soundPitch", s.soundPitch.toDouble())
        prefs.edit().putString("settings", o.toString()).apply()
    }

    // --- RESET & EXPORT / IMPORT ---
    fun resetAllData() {
        prefs.edit().clear().apply()
        seedInitialData()
        prefs.edit().putBoolean("seeded_v1", true).apply()
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("subjects", JSONArray(prefs.getString("subjects", "[]")))
        root.put("lectures", JSONArray(prefs.getString("lectures", "[]")))
        root.put("study_logs", JSONArray(prefs.getString("study_logs", "[]")))
        root.put("library_items", JSONArray(prefs.getString("library_items", "[]")))
        root.put("mistakes", JSONArray(prefs.getString("mistakes", "[]")))
        root.put("flashcards", JSONArray(prefs.getString("flashcards", "[]")))
        root.put("mock_tests", JSONArray(prefs.getString("mock_tests", "[]")))
        root.put("settings", JSONObject(prefs.getString("settings", "{}")))
        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val editor = prefs.edit()
            if (root.has("subjects")) editor.putString("subjects", root.getJSONArray("subjects").toString())
            if (root.has("lectures")) editor.putString("lectures", root.getJSONArray("lectures").toString())
            if (root.has("study_logs")) editor.putString("study_logs", root.getJSONArray("study_logs").toString())
            if (root.has("library_items")) editor.putString("library_items", root.getJSONArray("library_items").toString())
            if (root.has("mistakes")) editor.putString("mistakes", root.getJSONArray("mistakes").toString())
            if (root.has("flashcards")) editor.putString("flashcards", root.getJSONArray("flashcards").toString())
            if (root.has("mock_tests")) editor.putString("mock_tests", root.getJSONArray("mock_tests").toString())
            if (root.has("settings")) editor.putString("settings", root.getJSONObject("settings").toString())
            editor.apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun defaultSubjects(): List<Subject> {
        return listOf(
            Subject("sub_phy", "Physics", "#3B82F6"),
            Subject("sub_bot", "Botany", "#10B981"),
            Subject("sub_zoo", "Zoology", "#F59E0B"),
            Subject("sub_pchem", "Physical Chem", "#EC4899"),
            Subject("sub_ichem", "Inorganic Chem", "#8B5CF6"),
            Subject("sub_ochem", "Organic Chem", "#06B6D4")
        )
    }

    private fun seedInitialData() {
        val subjects = defaultSubjects()
        saveSubjects(subjects)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val now = System.currentTimeMillis()

        val sampleLectures = listOf(
            // Physics
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_phy",
                chapter = "Kinematics 1D & 2D",
                title = "Lec 01 - Vectors, Differentiation & Integration",
                durationMinutes = 90,
                speed = 1.5f,
                status = "done",
                flags = LectureFlags(dpp = true, notes = true, hw = true, extraQs = false),
                links = LectureLinks(dpp = "https://neetprep.com/dpp/kinematics-1", notes = "https://neetprep.com/notes/kin-1"),
                todayDate = null,
                priority = "high",
                completedAt = now - (86400000L * 2),
                revisionDay1 = true,
                revisionDay3 = false
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_phy",
                chapter = "Kinematics 1D & 2D",
                title = "Lec 02 - Projectile Motion on Horizontal & Incline",
                durationMinutes = 85,
                speed = 1.25f,
                status = "pending",
                flags = LectureFlags(dpp = false, notes = true, hw = false, extraQs = false),
                links = LectureLinks(notes = "https://drive.google.com/notes-projectile"),
                todayDate = todayStr,
                priority = "high"
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_phy",
                chapter = "Kinematics 1D & 2D",
                title = "Lec 03 - Relative Motion in 2D (Rain-Man & River-Boat)",
                durationMinutes = 80,
                speed = 1.5f,
                status = "pending",
                priority = "medium"
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_phy",
                chapter = "Laws of Motion",
                title = "Lec 01 - Newton's Laws & Free Body Diagrams",
                durationMinutes = 95,
                speed = 1.0f,
                status = "pending",
                priority = "high"
            ),

            // Botany
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_bot",
                chapter = "Cell: The Unit of Life",
                title = "Lec 01 - Cell Theory, Prokaryotic vs Eukaryotic",
                durationMinutes = 70,
                speed = 1.25f,
                status = "done",
                flags = LectureFlags(dpp = true, notes = true, hw = false, extraQs = false),
                todayDate = null,
                priority = "medium",
                completedAt = now - 86400000L,
                revisionDay1 = true
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_bot",
                chapter = "Cell: The Unit of Life",
                title = "Lec 02 - Endomembrane System: ER, Golgi & Lysosomes",
                durationMinutes = 65,
                speed = 1.5f,
                status = "in_progress",
                flags = LectureFlags(dpp = false, notes = false, hw = false, extraQs = false),
                todayDate = todayStr,
                priority = "high"
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_bot",
                chapter = "Principles of Inheritance",
                title = "Lec 01 - Mendel's Laws & Monohybrid Cross",
                durationMinutes = 80,
                speed = 1.25f,
                status = "pending",
                priority = "high"
            ),

            // Zoology
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_zoo",
                chapter = "Human Physiology: Digestion",
                title = "Lec 01 - Dental Formula & Histology of Gut",
                durationMinutes = 60,
                speed = 1.5f,
                status = "done",
                flags = LectureFlags(dpp = true, notes = true, hw = true, extraQs = true),
                todayDate = todayStr,
                priority = "medium",
                completedAt = now - (86400000L * 4),
                revisionDay1 = true,
                revisionDay3 = true
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_zoo",
                chapter = "Breathing & Exchange of Gases",
                title = "Lec 01 - Respiratory Volumes & Capacities",
                durationMinutes = 75,
                speed = 1.25f,
                status = "pending",
                todayDate = todayStr,
                priority = "high"
            ),

            // Physical Chem
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_pchem",
                chapter = "Chemical Kinetics",
                title = "Lec 01 - Rate Laws, Order & Molecularity",
                durationMinutes = 90,
                speed = 1.5f,
                status = "pending",
                flags = LectureFlags(dpp = false, notes = true, hw = false, extraQs = false),
                priority = "high"
            ),
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_pchem",
                chapter = "Chemical Kinetics",
                title = "Lec 02 - Integrated Rate Equations & Half Life",
                durationMinutes = 85,
                speed = 1.25f,
                status = "pending",
                priority = "medium"
            ),

            // Inorganic Chem
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_ichem",
                chapter = "Chemical Bonding",
                title = "Lec 01 - VSEPR Theory, Shapes & Dipole Moment",
                durationMinutes = 90,
                speed = 1.25f,
                status = "pending",
                priority = "high"
            ),

            // Organic Chem
            Lecture(
                id = UUID.randomUUID().toString(),
                subjectId = "sub_ochem",
                chapter = "General Organic Chemistry",
                title = "Lec 01 - Inductive & Resonance Effects",
                durationMinutes = 100,
                speed = 1.5f,
                status = "pending",
                todayDate = todayStr,
                priority = "high"
            )
        )
        saveLectures(sampleLectures)

        // Seed 7-day study logs
        val cal = Calendar.getInstance()
        val studyLogs = mutableListOf<StudyLogDay>()
        val dailyHours = listOf(5.2f, 6.5f, 4.0f, 7.0f, 6.2f, 5.8f, 4.5f)
        for (i in 6 downTo 0) {
            val d = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dStr = sdf.format(d.time)
            val mins = (dailyHours[6 - i] * 60).toInt()
            val subMap = mapOf(
                "sub_phy" to (mins * 0.35).toInt(),
                "sub_bot" to (mins * 0.25).toInt(),
                "sub_zoo" to (mins * 0.20).toInt(),
                "sub_pchem" to (mins * 0.20).toInt()
            )
            studyLogs.add(StudyLogDay(dStr, mins, subMap))
        }
        saveStudyLogs(studyLogs)

        // Library Items
        val library = listOf(
            LibraryItem(UUID.randomUUID().toString(), "DPP 01 - Vectors & Units", "https://neetprep.com/dpp1", "sub_phy", "DPP", true, 1, "Kinematics 1D & 2D"),
            LibraryItem(UUID.randomUUID().toString(), "DPP 02 - 1D Motion Graphs", "https://neetprep.com/dpp2", "sub_phy", "DPP", false, 2, "Kinematics 1D & 2D"),
            LibraryItem(UUID.randomUUID().toString(), "Class Notes - Cell Structure", "https://pw.live/notes/cell", "sub_bot", "Class notes", true, 1, "Cell: The Unit of Life"),
            LibraryItem(UUID.randomUUID().toString(), "HW Sheet - Mendel's Laws 50 Qs", "https://neetprep.com/hw/mendel", "sub_bot", "HW", false, 1, "Principles of Inheritance"),
            LibraryItem(UUID.randomUUID().toString(), "DPP 01 - Digestion & Absorption", "https://aakash.ac.in/dpp/dig1", "sub_zoo", "DPP", true, 1, "Human Physiology: Digestion"),
            LibraryItem(UUID.randomUUID().toString(), "Extra Qs - Rate Law Problems", "https://allen.in/chem-kin-extra", "sub_pchem", "Extra Qs", false, 1, "Chemical Kinetics"),
            LibraryItem(UUID.randomUUID().toString(), "Formula Sheet - VSEPR & Hybridization", "https://chemguide.co.uk/vsepr", "sub_ichem", "Class notes", false, 1, "Chemical Bonding"),
            LibraryItem(UUID.randomUUID().toString(), "GOC Reaction Intermediates PDF", "https://pw.live/goc-summary", "sub_ochem", "Class notes", false, 1, "General Organic Chemistry")
        )
        saveLibraryItems(library)

        // Mistakes
        val mistakes = listOf(
            MistakeItem(UUID.randomUUID().toString(), "sub_phy", "Kinematics: Forgot g component along inclined plane", "Calculation error", "Always resolve g into g*cos(θ) perpendicular and g*sin(θ) parallel to incline.", 1, todayStr),
            MistakeItem(UUID.randomUUID().toString(), "sub_bot", "Genetics: Dihybrid test cross ratio vs F2 phenotypic ratio", "Misread question", "Read question carefully: Test cross is 1:1:1:1, whereas F2 cross is 9:3:3:1.", 2, todayStr),
            MistakeItem(UUID.randomUUID().toString(), "sub_pchem", "Kinetics: Unit of rate constant k for 2nd order reaction", "Formula forgotten", "k = (mol/L)^(1-n) * s^(-1). For n=2, unit is L/(mol*s).", 0, todayStr)
        )
        saveMistakes(mistakes)

        // Flashcards
        val flashcards = listOf(
            Flashcard(UUID.randomUUID().toString(), "sub_phy", "Kinematics", "Centripetal Acceleration formula in circular motion?", "a_c = v^2 / r = ω^2 * r directed toward center.", true),
            Flashcard(UUID.randomUUID().toString(), "sub_bot", "Genetics", "What is the universal start codon in mRNA?", "AUG (codes for Methionine).", true),
            Flashcard(UUID.randomUUID().toString(), "sub_zoo", "Physiology", "Vital Capacity (VC) formula?", "VC = TV + IRV + ERV (Tidal Volume + Inspiratory Reserve + Expiratory Reserve).", false),
            Flashcard(UUID.randomUUID().toString(), "sub_pchem", "Solutions", "Henry's Law equation & statement?", "p = K_H * x (Partial pressure of gas in vapour phase is proportional to mole fraction x in solution).", false),
            Flashcard(UUID.randomUUID().toString(), "sub_ichem", "Bonding", "Hybridization and shape of XeF4 molecule?", "sp3d2 hybridization, Square Planar shape (2 axial lone pairs).", true),
            Flashcard(UUID.randomUUID().toString(), "sub_ochem", "GOC", "Stability order of carbocations?", "3° > 2° > 1° > methyl (stabilized by hyperconjugation & +I effect; resonance stabilized like tropylium even higher!).", false)
        )
        saveFlashcards(flashcards)

        // Mock Tests
        val mockTests = listOf(
            MockTest(UUID.randomUUID().toString(), "AITS Part Test 01", "2026-09-15", 145, 150, 320, "Physics silly errors in vectors. Biology was solid."),
            MockTest(UUID.randomUUID().toString(), "Full Syllabus Mock 01", "2026-10-02", 158, 162, 335, "Improved time management. Chem inorganic revision helped.")
        )
        saveMockTests(mockTests)

        saveSettings(AppSettings())
    }
}
