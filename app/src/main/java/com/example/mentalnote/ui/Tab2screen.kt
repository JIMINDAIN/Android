package com.example.mentalnote.ui

import com.example.mentalnote.R
import androidx.compose.runtime.Composable
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.GalleryTab
import com.example.mentalnote.ui.createSampleBitmap
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.graphics.Bitmap.Config
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap


fun createSampleBitmap(): ImageBitmap {
    val size = 200
    val bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = AndroidColor.YELLOW
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    return bitmap.asImageBitmap()
}

@Composable
fun Tab2Screen() {
    val sampleRecords = List(20) { index ->
        DayRecord(
            date = "2025-07-${(index + 1).toString().padStart(2, '0')}",
            emojiResID = when (index % 4) {
                0 -> R.drawable.emoji_happy
                1 -> R.drawable.emoji_bored
                2 -> R.drawable.emoji_blue
                else -> R.drawable.emoji_upset
            },
            summary = "샘플 요약 $index",
            detail = "샘플 상세 내용 $index",
            imageUriString = null,
            imageBitmap = createSampleBitmap()
        )
    }

    GalleryTab(dayRecords = sampleRecords)
}
