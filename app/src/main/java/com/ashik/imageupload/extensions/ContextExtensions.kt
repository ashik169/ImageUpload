import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ashik.imageupload.utils.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.getUriForFile(file: File): Uri = FileProvider.getUriForFile(
    applicationContext, FileUtils.APP_AUTHORITY, file
)

val Context.createImageFile: File
    @Throws(IOException::class) get() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        return File(filesDir, "IMG_${timeStamp}.jpg")
    }

@Throws(IOException::class)
fun Context.createCloudFile(fileName: String?): File {
    val cloudDir = File(filesDir, "cloud")
    if (!cloudDir.exists()) cloudDir.mkdirs()
    return if (fileName.isNullOrEmpty()) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        File(
            cloudDir, "IMG_${timeStamp}.jpg"
        )
    } else File(cloudDir, fileName)
}