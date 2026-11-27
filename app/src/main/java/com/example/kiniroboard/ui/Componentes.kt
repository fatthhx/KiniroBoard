package com.example.kiniroboard.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.ui.theme.*
import com.example.kiniroboard.util.cargarMiniatura
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Forma de flor para el botón "+" (como en tu boceto). */
class FlorShape(private val petalos: Int = 8) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radio = min(cx, cy)
        val pasos = 64
        for (i in 0..pasos) {
            val t = 2.0 * PI * i / pasos
            val r = radio * (0.9f + 0.1f * cos(petalos * t).toFloat())
            val x = cx + r * cos(t).toFloat()
            val y = cy + r * sin(t).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/** Encabezado en forma de píldora con el color de la categoría. */
@Composable
fun EncabezadoPill(titulo: String, color: Color, onVolver: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .border(2.dp, InkBlack, RoundedCornerShape(50))
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = InkBlack)
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
        Text(
            text = titulo,
            modifier = Modifier.weight(1f).semantics { heading() },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = InkBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(48.dp)) // equilibra la flecha para que el título quede centrado
    }
}

/** Portada de la obra; si no hay imagen, muestra el ícono de la categoría. */
@Composable
fun Portada(path: String?, categoria: Categoria, modifier: Modifier = Modifier) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, path) {
        value = path?.let { cargarMiniatura(it) }
    }
    val forma = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier.clip(forma).background(categoria.color).border(1.5.dp, InkBlack, forma),
        contentAlignment = Alignment.Center
    ) {
        val img = imagen
        if (img != null) {
            Image(bitmap = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(categoria.icono, contentDescription = null, tint = InkBlack.copy(alpha = 0.55f))
        }
    }
}

/** Botón "+" con forma de flor. Gira a "×" cuando el menú está abierto. */
@Composable
fun BotonFlor(abierto: Boolean, onClick: () -> Unit) {
    val rotacion by animateFloatAsState(if (abierto) 45f else 0f, label = "rotacionFlor")
    Surface(
        onClick = onClick,
        shape = FlorShape(),
        color = InkBlack,
        border = BorderStroke(2.dp, KiniroNeonGreen),
        modifier = Modifier
            .size(68.dp)
            .semantics { contentDescription = if (abierto) "Cerrar menú" else "Abrir menú: nuevo o cronología" }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = KiniroNeonGreen, modifier = Modifier.size(32.dp).rotate(rotacion))
        }
    }
}

/** Menú de tu boceto 7: el "+" despliega "Nuevo" y "Cronología". */
@Composable
fun MenuFlor(abierto: Boolean, onAlternar: () -> Unit, onNuevo: () -> Unit, onCronologia: () -> Unit) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedVisibility(
            visible = abierto,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OpcionMenu("Nuevo", Icons.Filled.Add, onNuevo)
                OpcionMenu("Cronología", Icons.Filled.Timeline, onCronologia)
            }
        }
        BotonFlor(abierto = abierto, onClick = onAlternar)
    }
}

@Composable
private fun OpcionMenu(texto: String, icono: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = YellowPastel,
        border = BorderStroke(2.dp, InkBlack),
        shadowElevation = 4.dp,
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = InkBlack, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, fontWeight = FontWeight.Bold, color = InkBlack)
        }
    }
}

/** Confirmación antes de borrar (prevención de errores). */
@Composable
fun DialogoConfirmar(titulo: String, mensaje: String, textoConfirmar: String, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = PaperCream,
        icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = RojoBorrar) },
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = InkBlack, textAlign = TextAlign.Center) },
        text = { Text(mensaje, color = InkBlack) },
        confirmButton = {
            Button(onClick = onConfirmar, colors = ButtonDefaults.buttonColors(containerColor = RojoBorrar, contentColor = Color.White)) {
                Text(textoConfirmar, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = InkBlack) } }
    )
}

/** Garabatos del fondo de inicio: estrellitas, espiral y línea punteada. */
@Composable
fun FondoGarabatos(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tinta = InkBlack.copy(alpha = 0.10f)
        val w = size.width
        val h = size.height

        fun destello(centro: Offset, r: Float) {
            val p = Path().apply {
                moveTo(centro.x, centro.y - r)
                quadraticTo(centro.x, centro.y, centro.x + r, centro.y)
                quadraticTo(centro.x, centro.y, centro.x, centro.y + r)
                quadraticTo(centro.x, centro.y, centro.x - r, centro.y)
                quadraticTo(centro.x, centro.y, centro.x, centro.y - r)
                close()
            }
            drawPath(p, tinta, style = Stroke(width = 4f))
        }

        destello(Offset(w * 0.86f, h * 0.10f), 46f)
        destello(Offset(w * 0.12f, h * 0.30f), 30f)
        destello(Offset(w * 0.90f, h * 0.45f), 26f)
        destello(Offset(w * 0.08f, h * 0.82f), 40f)

        for (i in 1..4) {
            drawCircle(tinta, radius = i * 34f, center = Offset(w * 0.82f, h * 0.66f), style = Stroke(width = 5f))
        }
        drawCircle(tinta, radius = 22f, center = Offset(w * 0.18f, h * 0.12f), style = Stroke(width = 4f))

        val curva = Path().apply {
            moveTo(0f, h * 0.55f)
            cubicTo(w * 0.25f, h * 0.45f, w * 0.35f, h * 0.70f, w * 0.60f, h * 0.60f)
            cubicTo(w * 0.80f, h * 0.52f, w * 0.85f, h * 0.90f, w, h * 0.88f)
        }
        drawPath(curva, tinta, style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f))))
    }
}

/** Colores comunes de los campos de texto (se usan en la Fase 6). */
@Composable
fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LilacDeep,
    unfocusedBorderColor = InkBlack,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedLabelColor = LilacDeep,
    cursorColor = LilacDeep
)