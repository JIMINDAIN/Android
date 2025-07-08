package com.example.mentalnote

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
val USER_USERNAME = stringPreferencesKey("user_username")
val USER_PASSWORD = stringPreferencesKey("user_password")
val USER_FRIENDS = stringSetPreferencesKey("user_friends")