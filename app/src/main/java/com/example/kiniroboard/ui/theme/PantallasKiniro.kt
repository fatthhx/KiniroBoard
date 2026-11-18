package com.example.kiniroboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiniroboard.data.Coleccion
import com.example.kiniroboard.data.Entrada
import com.example.kiniroboard.ui.theme.*
import com.example.kiniroboard.viewmodel.KiniroViewModel

fun obtenerColorTarjeta(tema: String): Color {
    return when (tema) {
        "VERDE_KINIRO" -> KiniroSoftGreen
        "LILA_PASTEL" -> LilacPastel
        "ROSA_PASTEL" -> PinkPastel
        "AMARILLO_PASTEL" -> YellowPastel
        else -> KiniroSoftGreen
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTableroPrincipal(
    viewModel: KiniroViewModel,
    onSeleccionarColeccion: (Int) -> Unit
) {
    val colecciones by viewModel.colecciones.collectAsState()
    val filtroActual by viewModel.filtroCategoria.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var coleccionAEditar by remember { mutableStateOf<Coleccion?>(null) }

    val categorias = listOf("Todos", "Manga", "Anime", "Música", "Libros", "Películas", "Hobby")

    Scaffold(
        containerColor = PaperCream,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InkBlack)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(KiniroNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KINIRO NOTES // BOARD",
                        color = KiniroNeonGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.2.sp
                    )
                }
                Text(
                    text = "Tu archivo personal de mangas, música, cine y hobbies",
                    color = LilacPastel,
                    fontSize = 12.sp
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    coleccionAEditar = null
                    mostrarDialogo = true
                },
                containerColor = InkBlack,
                contentColor = KiniroNeonGreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(2.dp, KiniroNeonGreen, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo tablero")
                Spacer(Modifier.width(6.dp))
                Text("Nuevo Tablero", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    val seleccionado = filtroActual == cat
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.cambiarFiltro(cat) },
                        label = {
                            Text(
                                text = cat,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = KiniroNeonGreen,
                            containerColor = Color.White,
                            labelColor = InkBlack
                        ),
                        border = BorderStroke(1.5.dp, InkBlack),
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            if (colecciones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LilacDeep,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Aún no tienes tableros aquí",
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            "Toca '+ Nuevo Tablero' para empezar tu colección",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(colecciones, key = { it.id }) { item ->
                        TarjetaPinterestColeccion(
                            coleccion = item,
                            onClick = { onSeleccionarColeccion(item.id) },
                            onEditar = {
                                coleccionAEditar = item
                                mostrarDialogo = true
                            },
                            onEliminar = { viewModel.borrarColeccion(item) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoFormularioColeccion(
            coleccionInicial = coleccionAEditar,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nuevaColeccion ->
                viewModel.guardarColeccion(nuevaColeccion)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun TarjetaPinterestColeccion(
    coleccion: Coleccion,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fondoCard = obtenerColorTarjeta(coleccion.temaColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = fondoCard),
        border = BorderStroke(2.dp, BorderInk),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = InkBlack,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = coleccion.categoria.uppercase(),
                    color = KiniroNeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = coleccion.titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = InkBlack,
                lineHeight = 21.sp
            )

            if (coleccion.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = coleccion.descripcion,
                    fontSize = 13.sp,
                    color = Color(0xFF2B2B30),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderInk.copy(alpha = 0.25f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar tablero",
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar tablero",
                        tint = Color(0xFFB00020),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleColeccion(
    coleccionId: Int,
    viewModel: KiniroViewModel,
    onVolver: () -> Unit
) {
    val coleccion by viewModel.obtenerColeccionPorId(coleccionId).collectAsState()
    val entradas by viewModel.obtenerEntradasDeColeccion(coleccionId).collectAsState()

    var mostrarDialogoEntrada by remember { mutableStateOf(false) }
    var entradaAEditar by remember { mutableStateOf<Entrada?>(null) }

    val colorCabecera = coleccion?.let { obtenerColorTarjeta(it.temaColor) } ?: KiniroSoftGreen

    Scaffold(
        containerColor = PaperCream,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = coleccion?.titulo ?: "Cargando...",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = InkBlack
                        )
                        Text(
                            text = "${entradas.size} notas guardadas • ${coleccion?.categoria ?: ""}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = InkBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorCabecera)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    entradaAEditar = null
                    mostrarDialogoEntrada = true
                },
                containerColor = LilacDeep,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.border(2.dp, InkBlack, CircleShape)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = "Agregar nota")
            }
        }
    ) { padding ->
        if (entradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Esta libreta está vacía.\nToca el botón morado para escribir tu primera nota.",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entradas, key = { it.id }) { entrada ->
                    TarjetaNotaEntrada(
                        entrada = entrada,
                        onEditar = {
                            entradaAEditar = entrada
                            mostrarDialogoEntrada = true
                        },
                        onEliminar = { viewModel.borrarEntrada(entrada) }
                    )
                }
            }
        }
    }

    if (mostrarDialogoEntrada) {
        DialogoFormularioEntrada(
            coleccionId = coleccionId,
            entradaInicial = entradaAEditar,
            onDismiss = { mostrarDialogoEntrada = false },
            onGuardar = { nuevaEntrada ->
                viewModel.guardarEntrada(nuevaEntrada)
                mostrarDialogoEntrada = false
            }
        )
    }
}

@Composable
fun TarjetaNotaEntrada(
    entrada: Entrada,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.8.dp, InkBlack)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entrada.titulo,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = YellowPastel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, InkBlack)
                ) {
                    Text(
                        text = "★ ${entrada.calificacion}/10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = InkBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (entrada.progreso.isNotBlank()) {
                    Text(
                        text = "📌 ${entrada.progreso}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LilacDeep
                    )
                }
                if (entrada.fecha.isNotBlank()) {
                    Text(
                        text = "• ${entrada.fecha}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaperCream)
                    .padding(10.dp)
            ) {
                Text(
                    text = entrada.notaDetalle,
                    fontSize = 14.sp,
                    color = InkBlack,
                    fontStyle = FontStyle.Normal
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", color = InkBlack)
                }
                TextButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFB00020),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Borrar", color = Color(0xFFB00020))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFormularioColeccion(
    coleccionInicial: Coleccion?,
    onDismiss: () -> Unit,
    onGuardar: (Coleccion) -> Unit
) {
    var titulo by remember { mutableStateOf(coleccionInicial?.titulo ?: "") }
    var categoria by remember { mutableStateOf(coleccionInicial?.categoria ?: "Manga") }
    var descripcion by remember { mutableStateOf(coleccionInicial?.descripcion ?: "") }
    var temaColor by remember { mutableStateOf(coleccionInicial?.temaColor ?: "VERDE_KINIRO") }
    var errorMensaje by remember { mutableStateOf("") }

    val opcionesCategoria = listOf("Manga", "Anime", "Música", "Libros", "Películas", "Hobby")
    val colores = listOf(
        "VERDE_KINIRO" to "Verde Kiniro",
        "LILA_PASTEL" to "Lila",
        "ROSA_PASTEL" to "Rosa",
        "AMARILLO_PASTEL" to "Amarillo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperCream,
        title = {
            Text(
                text = if (coleccionInicial == null) "Nuevo Tablero" else "Editar Tablero",
                fontWeight = FontWeight.Black,
                color = InkBlack
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it; errorMensaje = "" },
                    label = { Text("Título del tablero*") },
                    placeholder = { Text("Ej: Mangas Yuri / Rock 90s") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(opcionesCategoria) { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción o vibra del tablero") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Color de tarjeta:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colores.forEach { (clave, _) ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(obtenerColorTarjeta(clave))
                                .border(
                                    width = if (temaColor == clave) 3.dp else 1.dp,
                                    color = InkBlack,
                                    shape = CircleShape
                                )
                                .clickable { temaColor = clave }
                        )
                    }
                }

                if (errorMensaje.isNotEmpty()) {
                    Text(errorMensaje, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkBlack,
                    contentColor = KiniroNeonGreen
                ),
                onClick = {
                    if (titulo.isBlank()) {
                        errorMensaje = "El título no puede estar vacío."
                    } else {
                        onGuardar(
                            Coleccion(
                                id = coleccionInicial?.id ?: 0,
                                titulo = titulo.trim(),
                                categoria = categoria,
                                descripcion = descripcion.trim(),
                                temaColor = temaColor
                            )
                        )
                    }
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = InkBlack) }
        }
    )
}

@Composable
fun DialogoFormularioEntrada(
    coleccionId: Int,
    entradaInicial: Entrada?,
    onDismiss: () -> Unit,
    onGuardar: (Entrada) -> Unit
) {
    var titulo by remember { mutableStateOf(entradaInicial?.titulo ?: "") }
    var notaDetalle by remember { mutableStateOf(entradaInicial?.notaDetalle ?: "") }
    var fecha by remember { mutableStateOf(entradaInicial?.fecha ?: "16/11/2026") }
    var progreso by remember { mutableStateOf(entradaInicial?.progreso ?: "") }
    var calificacionTexto by remember {
        mutableStateOf(entradaInicial?.calificacion?.toString() ?: "9.5")
    }
    var errorMensaje by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperCream,
        title = {
            Text(
                text = if (entradaInicial == null) "Nueva Nota / Pin" else "Editar Nota",
                fontWeight = FontWeight.Black,
                color = InkBlack
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it; errorMensaje = "" },
                    label = { Text("Nombre (Obra, canción, libro...)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = progreso,
                    onValueChange = { progreso = it },
                    label = { Text("Progreso / Dato clave") },
                    placeholder = { Text("Ej: Cap. 42, Min 1:45, Terminado") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calificacionTexto,
                        onValueChange = { calificacionTexto = it; errorMensaje = "" },
                        label = { Text("Nota (0-10)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
                OutlinedTextField(
                    value = notaDetalle,
                    onValueChange = { notaDetalle = it; errorMensaje = "" },
                    label = { Text("Apuntes / Reseña personal*") },
                    placeholder = { Text("Escribe lo que más te gustó, citas o pensamientos...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMensaje.isNotEmpty()) {
                    Text(errorMensaje, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacDeep,
                    contentColor = Color.White
                ),
                onClick = {
                    val califNumero = calificacionTexto.toDoubleOrNull()
                    when {
                        titulo.isBlank() || notaDetalle.isBlank() -> {
                            errorMensaje = "Completa el título y tus apuntes."
                        }
                        califNumero == null || califNumero < 0.0 || califNumero > 10.0 -> {
                            errorMensaje = "La calificación debe ser un número válido entre 0 y 10."
                        }
                        else -> {
                            onGuardar(
                                Entrada(
                                    id = entradaInicial?.id ?: 0,
                                    coleccionId = coleccionId,
                                    titulo = titulo.trim(),
                                    notaDetalle = notaDetalle.trim(),
                                    fecha = fecha.trim(),
                                    progreso = progreso.trim(),
                                    calificacion = califNumero
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Guardar Nota", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = InkBlack) }
        }
    )
}