package com.example.mentalnote.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mentalnote.model.DayRecord


@Composable
fun GalleryTab(dayRecords: List<DayRecord>) {
    Column{
        AppHeader()

        val context = LocalContext.current

        // Tab1에서 저장된 기록 중 사진이 있는 항목만 필터링
        val photoRecords = remember(dayRecords) {
            dayRecords.filter { it.imageUri != null || it.imageBitmap != null }
                .sortedByDescending { it.date }
        }

        if (photoRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("저장된 사진이 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photoRecords.size) { index ->
                    val record = photoRecords[index]

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        when {
                            record.imageBitmap != null -> {
                                Image(
                                    bitmap = record.imageBitmap,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            record.imageUri != null -> {
                                val bitmap = remember(record.imageUri) {
                                    val inputStream = context.contentResolver.openInputStream(record.imageUri)
                                    val bmp = BitmapFactory.decodeStream(inputStream)
                                    inputStream?.close()
                                    bmp
                                }
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
