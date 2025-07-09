package com.example.mentalnote.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Transient

/**
 * 요일별 기록을 나타내는 데이터 클래스
 *
 * @property date 날짜 (i.e. 2025-07-04)
 * @property emoji 기분 이모지 (Default: "")
 * @property summary 한 줄 요약 (Default: "")
 * @property detail 상세 기록 (Default: "")
 * @property imageUri 갤러리에서 선택한 이미지의 URI (Default: null)
 * @property imageBitmap 카메라로 촬영한 이미지의 Bitmap (Default: null)
 */

@Serializable
data class DayRecord(
    val date: String = "", //yyyy-mm-dd
    val emojiResID: Int? = null,
    val summary: String = "",
    val detail: String = "",
    val imageUri: String? = null,

    @Transient
    val imageBitmap: ImageBitmap? = null
)
