package com.example.mentalnote.model

import kotlinx.serialization.Serializable

@Serializable
data class DummyRecord(
    val date: String,
    val emojiResName: String,
    val summary: String,
    val detail: String,
    val imageFileName: String? = null
)
