// DummyDataLoader.kt
package com.example.mentalnote.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mentalnote.dataStore
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.saveDayRecords
import copyAssetToCache
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val DUMMY_LOADED_KEY = stringPreferencesKey("dummy_loaded")

suspend fun loadDummyDataIfNeeded(context: Context) {
    val prefs = context.dataStore.data.first()
    val alreadyLoaded = prefs[DUMMY_LOADED_KEY] == "true"

    if (alreadyLoaded) return

    val records = mutableListOf<DayRecord>()

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    val dummyFiles = listOf(
        "images/img_1.png",
        "images/img_2.png",
        "images/img_3.png",
        "images/img_4.png",
        "images/img_5.jpg",
        "images/img_6.jpg",
        "images/img_7.jpg"
    )


    for ((i, assetPath) in dummyFiles.withIndex()) {
        val date = today.minusDays(i.toLong())
        val uri = copyAssetToCache(
            context,
            assetPath,
            "dummy_${i + 1}.${assetPath.substringAfterLast('.')}"
        )
        records.add(
            DayRecord(
                date = date.format(formatter),
                emojiResID = null,
                summary = "임시 요약 ${i + 1}",
                detail = "임의로 생성한 과거 감정 기록 상세 내용 ${i + 1}",
                imageUri = uri
            )
        )
    }

    saveDayRecords(context, records)

    // 저장 완료 여부 기록
    context.dataStore.edit { settings ->
        settings[DUMMY_LOADED_KEY] = "true"
    }
}
