package com.example.mentalnote.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun copyAssetToCache(context: Context, assetPath: String, filename: String): Uri {
    val file = File(context.cacheDir, filename)
    if (!file.exists()) {
        context.assets.open(assetPath).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}