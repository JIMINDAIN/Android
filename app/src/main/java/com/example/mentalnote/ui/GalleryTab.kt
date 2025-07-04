package com.example.mentalnote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mentalnote.model.DayRecord

@Composable
fun GalleryTab(dayRecords: List<DayRecord>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gallery 탭은 아직 구현 중입니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
