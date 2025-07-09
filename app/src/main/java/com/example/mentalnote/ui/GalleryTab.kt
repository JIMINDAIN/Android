package com.example.mentalnote.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.mentalnote.model.DayRecord


@Composable
fun GalleryTab(dayRecords: List<DayRecord>) {
    Column{
        AppHeader()

        Spacer(modifier = Modifier.height(8.dp))

        val context = LocalContext.current

        // Tab1에서 저장된 기록 중 사진이 있는 항목만 필터링
        val photoRecords = remember(dayRecords) {
            dayRecords.filter { it.imageUri != null || it.imageBitmap != null }
                .sortedByDescending { it.date }
            //.reversed()
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
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        when {
                            record.imageBitmap != null -> {
                                Image(
                                    bitmap = record.imageBitmap,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            record.imageUri != null -> {
                                val bitmap = remember(record.imageUri) {
                                    try{
                                        val uri = Uri.parse(record.imageUri.toString())
                                        val options = BitmapFactory.Options().apply {
                                            inSampleSize = 4
                                        }

                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val bmp = BitmapFactory.decodeStream(inputStream, null, options)
                                        inputStream?.close()

                                        val exifStream = context.contentResolver.openInputStream(uri)
                                        val exif = ExifInterface(exifStream!!)
                                        val orientation = exif.getAttributeInt(
                                            ExifInterface.TAG_ORIENTATION,
                                            ExifInterface.ORIENTATION_NORMAL
                                        )

                                        val matrix = Matrix()
                                        when (orientation) {
                                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                                        }
                                        exifStream.close()

                                        bmp?.let { Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true) }

                                    } catch(e: Exception){
                                        e.printStackTrace()
                                        null
                                    }

                                    //bmp
                                }
                                bitmap?.let {
                                    Image(
                                        //bitmap = it.asImageBitmap(),
                                        painter = rememberAsyncImagePainter(model = Uri.parse(record.imageUri)),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
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