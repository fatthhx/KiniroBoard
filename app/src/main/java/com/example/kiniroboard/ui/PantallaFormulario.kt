package com.example.kiniroboard.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.data.ultimoVisto
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.ui.theme.*
import com.example.kiniroboard.util.borrarImagen
import com.example.kiniroboard.util.copiarImagenAlApp
import com.example.kiniroboard.util.formatearFechaUtc
import com.example.kiniroboard.viewmodel.KiniroViewModel
import kotlinx.coroutines.launch

private const val MAX_CAPITULOS = 5000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    categoria: Categoria,
    obraId: Int?,                 // null = crear nueva · número = editar esa obra
    viewModel: KiniroViewModel,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val esEdicion = obraId != null

    // ---------- Lo que el usuario escribe (sobrevive si gira el teléfono) ----------
    var titulo by rememberSaveable { mutableStateOf("") }
    var totalTexto by rememberSaveable { mutableStateOf(if (categoria == Categoria.PELICULAS) "1" else "") }
    var plataforma by rememberSaveable { mutableStateOf("") }
    var imagenPath by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaInicio by rememberSaveable { mutableStateOf<Long?>(null) }

    // ---------- Datos originales (solo al editar) ----------
    var cargado by rememberSaveable { mutableStateOf(!esEdicion) }
    var imagenOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaRegistroOriginal by rememberSaveable { mutableStateOf<Long?>(null) }
    var ultimoVisto by rememberSaveable { mutableIntStateOf(0) }

    var intentoGuardar by rememberSaveable { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var copiandoImagen by remember { mutableStateOf(false) }

    // Si es edición, carga la obra UNA vez y llena los campos
    LaunchedEffect(obraId) {
        if (obraId != null && !cargado) {
            viewModel.obtenerObra(obraId)?.let { o ->
                titulo = o.obra.titulo
                totalTexto = o.obra.totalCapitulos.toString()
                plataforma = o.obra.plataforma
                imagenPath = o.obra.imagenPath
                imagenOriginal = o.obra.imagenPath
                fechaInicio = o.obra.fechaInicio
                fechaRegistroOriginal = o.obra.fechaRegistro
                ultimoVisto = o.ultimoVisto
            }
            cargado = true
        }
    }

    // ---------- Validación (se recalcula sola en cada tecla) ----------
    val total = totalTexto.toIntOrNull()
    val errorTitulo = if (titulo.isBlank()) "Escribe el nombre" else null
    val errorTotal = when {
        total == null || total < 1 -> "Escribe cuántos ${categoria.unidad.lowercase()} tiene (mínimo 1)"
        total > MAX_CAPITULOS -> "Máximo $MAX_CAPITULOS"
        total < ultimoVisto -> "Ya marcaste hasta el ${categoria.unidadSingular.lowercase()} $ultimoVisto; no puede ser menor"
        else -> null
    }
    val mostrarErrorTitulo = intentoGuardar && errorTitulo != null
    val mostrarErrorTotal = (intentoGuardar || totalTexto.isNotEmpty()) && errorTotal != null

    // Al cancelar, borra la imagen nueva que ya no se usará
    fun cancelar() {
        imagenPath?.let { if (it != imagenOriginal) borrarImagen(it) }
        onCerrar()
    }
    BackHandler { cancelar() }

    // Selector de fotos del sistema (no necesita pedir permisos)
    val selectorImagen = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            copiandoImagen = true
            scope.launch {
                val nueva = copiarImagenAlApp(context, uri)
                if (nueva != null) {
                    imagenPath?.let { if (it != imagenOriginal) borrarImagen(it) }
                    imagenPath = nueva
                }
                copiandoImagen = false
            }
        }
    }

    Scaffold(
        containerColor = PaperCream,
        topBar = {
            EncabezadoPill(
                titulo = if (esEdicion) "Editar · ${categoria.nombre}" else "Nuevo · ${categoria.nombre}",
                color = categoria.color,
                onVolver = { cancelar() }
            )
        }
    ) { padding ->
        if (!cargado) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LilacDeep)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()                       // el teclado no tapa los campos
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ----- Nombre -----
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Nombre*") },
                placeholder = { Text("Ej: Kiniro Mosaic") },
                singleLine = true,
                isError = mostrarErrorTitulo,
                supportingText = { if (mostrarErrorTitulo) Text(errorTitulo!!) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                colors = coloresCampo(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // ----- Capítulos / Episodios con − y + -----
            Row(verticalAlignment = Alignment.Top) {
                IconButton(
                    onClick = { totalTexto = ((total ?: 2) - 1).coerceAtLeast(1).toString() },
                    enabled = (total ?: 0) > 1,
                    modifier = Modifier.padding(top = 8.dp)
                ) { Icon(Icons.Filled.Remove, contentDescription = "Uno menos") }

                OutlinedTextField(
                    value = totalTexto,
                    onValueChange = { nuevo ->
                        if (nuevo.length <= 4 && nuevo.all { it.isDigit() }) totalTexto = nuevo
                    },
                    label = { Text("${categoria.unidad}*") },
                    singleLine = true,
                    isError = mostrarErrorTotal,
                    supportingText = {
                        if (mostrarErrorTotal) Text(errorTotal!!)
                        else if (categoria == Categoria.PELICULAS) Text("Ej: una trilogía tiene 3 partes")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    colors = coloresCampo(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { totalTexto = ((total ?: 0) + 1).coerceAtMost(MAX_CAPITULOS).toString() },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Uno más") }
            }

            // ----- Plataforma -----
            OutlinedTextField(
                value = plataforma,
                onValueChange = { plataforma = it },
                label = { Text("Plataforma (opcional)") },
                placeholder = {
                    Text(
                        when (categoria) {
                            Categoria.LIBROS, Categoria.COMICS, Categoria.MANGAS -> "Ej: Físico, Kindle, Webtoon"
                            else -> "Ej: Crunchyroll, Netflix, Cine"
                        }
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                colors = coloresCampo(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // ----- Fecha de inicio -----
            Text("¿Cuándo empezaste? (opcional)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkBlack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { mostrarCalendario = true },
                    border = BorderStroke(1.5.dp, InkBlack),
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = LilacDeep)
                    Spacer(Modifier.width(8.dp))
                    Text(fechaInicio?.let { formatearFechaUtc(it) } ?: "Elegir fecha", color = InkBlack)
                }
                if (fechaInicio != null) {
                    IconButton(onClick = { fechaInicio = null }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Quitar fecha")
                    }
                }
            }

            // ----- Imagen -----
            Text("Portada (opcional)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkBlack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Portada(imagenPath, categoria, Modifier.size(96.dp))
                    if (copiandoImagen) CircularProgressIndicator(color = LilacDeep, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    OutlinedButton(
                        onClick = { selectorImagen.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        border = BorderStroke(1.5.dp, InkBlack)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = LilacDeep)
                        Spacer(Modifier.width(8.dp))
                        Text(if (imagenPath == null) "Cargar imagen" else "Cambiar imagen", color = InkBlack)
                    }
                    if (imagenPath != null) {
                        TextButton(onClick = {
                            imagenPath?.let { if (it != imagenOriginal) borrarImagen(it) }
                            imagenPath = null
                        }) { Text("Quitar imagen", color = RojoBorrar) }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ----- Guardar -----
            Button(
                onClick = {
                    intentoGuardar = true
                    if (errorTitulo == null && errorTotal == null && total != null && !copiandoImagen) {
                        viewModel.guardarObra(
                            obra = Obra(
                                id = obraId ?: 0,
                                categoria = categoria.name,
                                titulo = titulo.trim(),
                                totalCapitulos = total,
                                plataforma = plataforma.trim(),
                                imagenPath = imagenPath,
                                fechaInicio = fechaInicio,
                                fechaRegistro = fechaRegistroOriginal ?: System.currentTimeMillis()
                            ),
                            imagenAnterior = imagenOriginal
                        )
                        onCerrar()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = InkBlack, contentColor = KiniroNeonGreen),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
            ) {
                Text(if (esEdicion) "Guardar cambios" else "Guardar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    // ----- Calendario -----
    if (mostrarCalendario) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicio ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaInicio = estado.selectedDateMillis
                    mostrarCalendario = false
                }) { Text("Aceptar", color = LilacDeep, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar", color = InkBlack) }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}