package com.example.mentalnote.util

import android.content.Context
import com.example.mentalnote.R
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.model.DummyRecord
import kotlinx.serialization.json.Json

suspend fun loadDummyJsonRecords(context: Context): List<DayRecord> {
    val jsonStr = context.assets.open("dummydata/dummy.json")
        .bufferedReader()
        .use { it.readText() }

    val dummyList = Json.decodeFromString<List<DummyRecord>>(jsonStr)

    return dummyList.map { dummy ->
        val emojiResID = when (dummy.emojiResName) {
            "emoji_happy" -> R.drawable.emoji_happy
            "emoji_blue" -> R.drawable.emoji_blue
            "emoji_bored" -> R.drawable.emoji_bored
            "emoji_upset" -> R.drawable.emoji_upset
            else -> null
        }

        val imageUri = dummy.imageFileName?.let { fileName ->
            copyAssetToCache(context, "images/$fileName", "cached_$fileName")
        }

        DayRecord(
            date = dummy.date,
            emojiResID = emojiResID,
            summary = dummy.summary,
            detail = dummy.detail,
            imageUri = imageUri?.toString()
        )
    }
}
