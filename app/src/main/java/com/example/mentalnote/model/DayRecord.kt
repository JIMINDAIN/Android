package com.example.mentalnote.model

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 요일별 기록을 나타내는 데이터 클래스
 *
 * @property day 요일 (예: "월")
 * @property emoji 기분 이모지 (예: "😃")
 * @property summary 한 줄 요약
 * @property detail 상세 기록 (기본값: 빈 문자열)
 * @property imageUri 갤러리에서 선택한 이미지의 URI (nullable)
 * @property imageBitmap 카메라로 촬영한 이미지의 Bitmap (nullable)
 */
data class DayRecord(
    val day: String,
    val emoji: String,
    val summary: String,
    val detail: String = "",
    val imageUri: Uri? = null,
    val imageBitmap: ImageBitmap? = null
)
