package com.example.mentalnote

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_prefs")
val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
