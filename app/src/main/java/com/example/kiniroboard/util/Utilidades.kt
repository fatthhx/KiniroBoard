package com.example.kiniroboard.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private val localeMx: Locale = Locale.forLanguageTag("es-MX")

// Copia la imagen de la galería a la carpeta privada de la app
suspend fun copiarImagenAlApp(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    runCatching {
        val carpeta = File(context.filesDir, "portadas").apply { mkdirs() }
        val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
        val entrada = context.contentResolver.openInputStream(uri) ?: return@runCatching null
        entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }
        destino.absolutePath
    }.getOrNull()
}

// Carga la imagen en tamaño reducido para no gastar memoria
suspend fun cargarMiniatura(path: String, ladoMaximo: Int = 300): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, medidas)
        var escala = 1
        while (medidas.outWidth / (escala * 2) >= ladoMaximo && medidas.outHeight / (escala * 2) >= ladoMaximo) {
            escala *= 2
        }
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = escala })?.asImageBitmap()
    }.getOrNull()
}

fun borrarImagen(path: String) {
    runCatching { File(path).delete() }
}

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("d 'de' MMMM yyyy", localeMx).format(Date(millis))

fun formatearHora(millis: Long): String =
    SimpleDateFormat("HH:mm", localeMx).format(Date(millis))

// El DatePicker de Material 3 entrega la fecha en UTC
fun formatearFechaUtc(millis: Long): String =
    SimpleDateFormat("d 'de' MMMM yyyy", localeMx).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))