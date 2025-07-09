
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mentalnote.R
import com.example.mentalnote.dataStore
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.model.DummyRecord
import com.example.mentalnote.util.copyAssetToCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

val DUMMY_LOADED_KEY = stringPreferencesKey("dummy_loaded")

suspend fun loadDummyJsonRecordsOnce(context: Context): List<DayRecord> {
    val prefs = context.dataStore.data.first()
    val alreadyLoaded = prefs[DUMMY_LOADED_KEY] ?: "false"

    if (alreadyLoaded == "true") return emptyList()

    val dummyList = withContext(Dispatchers.IO) {
        val jsonStr = context.assets.open("dummydata/dummy.json")
            .bufferedReader()
            .use { it.readText() }

        Json.decodeFromString<List<DummyRecord>>(jsonStr)
    }

    val mappedList = dummyList.map { dummy ->
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
            imageUri = imageUri
        )
    }

    context.dataStore.edit { prefs ->
        prefs[DUMMY_LOADED_KEY] = "true"
    }

    return mappedList
}
