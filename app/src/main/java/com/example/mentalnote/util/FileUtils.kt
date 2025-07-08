import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File


fun copyAssetToCache(context: Context, assetPath: String, outputFileName : String) : Uri {
    val inputStream = context.assets.open(assetPath)
    val file = File(context.cacheDir, outputFileName)
    inputStream.use { input ->
        file.outputStream().use{ output ->
            input.copyTo(output)
        }
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
