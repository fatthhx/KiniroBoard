package com.example.kiniroboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiniroboard.data.Capitulo
import com.example.kiniroboard.data.ObraConCapitulos
import com.example.kiniroboard.data.estado
import com.example.kiniroboard.data.progreso
import com.example.kiniroboard.data.vistos
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.model.Estado
import com.example.kiniroboard.model.Filtro
import com.example.kiniroboard.model.etiqueta
import com.example.kiniroboard.ui.theme.*
import com.example.kiniroboard.util.formatearFechaUtc
import com.example.kiniroboard.viewmodel.KiniroViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PantallaCategoria(
    categoria: Categoria,
    viewModel: KiniroViewModel,
    onVolver: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    onCronologia: () -> Unit
) {
    // Le avisa al ViewModel qué categoría se abrió
    LaunchedEffect(categoria) { viewModel.seleccionarCategoria(categoria) }

    val todas by viewModel.obras.collectAsState()
    val obras = todas.filter { it.obra.categoria == categoria.name }
    val filtro by viewModel.filtro.collectAsState()

    // Estado propio de la pantalla
    var menuAbierto by remember { mutableStateOf(false) }
    var expandida by rememberSaveable { mutableStateOf<Int?>(null) }
    var porEliminar by remember { mutableStateOf<ObraConCapitulos?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Si el menú está abierto, "atrás" lo cierra en vez de salir
    BackHandler(enabled = menuAbierto) { menuAbierto = false }

    Scaffold(
        containerColor = PaperCream,
        topBar = { EncabezadoPill(categoria.nombre, categoria.color, onVolver) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            MenuFlor(
                abierto = menuAbierto,
                onAlternar = { menuAbierto = !menuAbierto },
                onNuevo = { menuAbierto = false; onNuevo() },
                onCronologia = { menuAbierto = false; onCronologia() }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            BarraFiltros(categoria, filtro) { viewModel.cambiarFiltro(it) }

            if (obras.isEmpty()) {
                EstadoVacio(categoria, filtro, onNuevo) { viewModel.cambiarFiltro(Filtro.TODOS) }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(obras, key = { it.obra.id }) { item ->
                        TarjetaObra(
                            item = item,
                            categoria = categoria,
                            expandida = expandida == item.obra.id,
                            onAlternarExpandir = {
                                expandida = if (expandida == item.obra.id) null else item.obra.id
                            },
                            onCirculo = {
                                val antes = item.capitulos               // copia para "Deshacer"
                                val completar = item.estado != Estado.FINALIZADO
                                viewModel.marcarTodo(item, completar)
                                scope.launch {
                                    snackbar.currentSnackbarData?.dismiss()
                                    val mensaje = if (completar)
                                        "“${item.obra.titulo}” ${categoria.genero("terminado", "terminada")}"
                                    else "Se reinició el progreso de “${item.obra.titulo}”"
                                    val resultado = snackbar.showSnackbar(
                                        message = mensaje,
                                        actionLabel = "Deshacer",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (resultado == SnackbarResult.ActionPerformed) {
                                        viewModel.restaurarCapitulos(antes)
                                    }
                                }
                            },
                            onCapitulo = { viewModel.alternarCapitulo(it) },
                            onEditar = { onEditar(item.obra.id) },
                            onEliminar = { porEliminar = item }
                        )
                    }
                }
            }
        }
    }

    // Confirmación antes de borrar
    porEliminar?.let { o ->
        DialogoConfirmar(
            titulo = "¿Eliminar “${o.obra.titulo}”?",
            mensaje = "También se borrará su progreso (${o.vistos} de ${o.obra.totalCapitulos} " +
                    "${categoria.unidad.lowercase()} marcados). Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminarObra(o.obra)
                if (expandida == o.obra.id) expandida = null
                porEliminar = null
            },
            onCancelar = { porEliminar = null }
        )
    }
}

/** Pestañas: Por ver | Empezados | Finalizados | Todos */
@Composable
private fun BarraFiltros(categoria: Categoria, seleccionado: Filtro, onCambiar: (Filtro) -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(forma)
            .background(Color.White)
            .border(2.dp, InkBlack, forma)
            .selectableGroup()
    ) {
        Filtro.entries.forEach { f ->
            val activo = f == seleccionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .background(if (activo) InkBlack else Color.Transparent)
                    .selectable(selected = activo, role = Role.Tab, onClick = { onCambiar(f) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    f.etiqueta(categoria),
                    color = if (activo) KiniroNeonGreen else InkBlack,
                    fontSize = 12.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EstadoVacio(categoria: Categoria, filtro: Filtro, onNuevo: () -> Unit, onVerTodo: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(categoria.icono, contentDescription = null, tint = LilacDeep, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(10.dp))
        if (filtro == Filtro.TODOS) {
            Text("Todavía no hay nada en ${categoria.nombre}", fontWeight = FontWeight.Bold, color = InkBlack, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNuevo, colors = ButtonDefaults.buttonColors(containerColor = InkBlack, contentColor = KiniroNeonGreen)) {
                Text(categoria.genero("Agregar el primero", "Agregar la primera"), fontWeight = FontWeight.Bold)
            }
        } else {
            Text("No hay nada en “${filtro.etiqueta(categoria)}”", fontWeight = FontWeight.Bold, color = InkBlack, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onVerTodo, border = BorderStroke(1.5.dp, InkBlack)) {
                Text("Ver ${Filtro.TODOS.etiqueta(categoria).lowercase()}", color = InkBlack)
            }
        }
    }
}

@Composable
private fun TarjetaObra(
    item: ObraConCapitulos,
    categoria: Categoria,
    expandida: Boolean,
    onAlternarExpandir: () -> Unit,
    onCirculo: () -> Unit,
    onCapitulo: (Capitulo) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val obra = item.obra
    val resumen = buildString {
        if (obra.totalCapitulos > 1) {
            append("${categoria.unidadCorta} ${item.vistos} de ${obra.totalCapitulos}")
        } else {
            append(if (item.estado == Estado.FINALIZADO) categoria.genero("Terminado", "Terminada") else categoria.pendiente)
        }
        if (obra.plataforma.isNotBlank()) append(" · ${obra.plataforma}")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.8.dp, InkBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = if (expandida) "Ocultar detalle" else "Ver progreso", onClick = onAlternarExpandir)
                .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CirculoEstado(item, categoria, onCirculo)
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(obra.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                HorizontalDivider(color = InkBlack, thickness = 1.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                Text(resumen, fontSize = 12.sp, color = InkBlack.copy(alpha = 0.7f))
            }
            Portada(obra.imagenPath, categoria, Modifier.size(64.dp))
        }

        AnimatedVisibility(visible = expandida) {
            DetalleObra(item, categoria, onCapitulo, onEditar, onEliminar)
        }
    }
}

/** Círculo vacío = pendiente · anillo = en progreso · ⊗ = terminado */
@Composable
private fun CirculoEstado(item: ObraConCapitulos, categoria: Categoria, onClick: () -> Unit) {
    val titulo = item.obra.titulo
    val terminar = "Marcar “$titulo” como ${categoria.genero("terminado", "terminada")}"
    val (estadoTexto, accion) = when (item.estado) {
        Estado.POR_VER -> categoria.pendiente to terminar
        Estado.EMPEZADO -> "${(item.progreso * 100).roundToInt()}% completado" to terminar
        Estado.FINALIZADO -> categoria.genero("Terminado", "Terminada") to "Reiniciar progreso de “$titulo”"
    }
    IconButton(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = accion
            stateDescription = estadoTexto
        }
    ) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            when (item.estado) {
                Estado.POR_VER -> Box(Modifier.fillMaxSize().border(2.dp, InkBlack, CircleShape))
                Estado.EMPEZADO -> CircularProgressIndicator(
                    progress = { item.progreso },
                    modifier = Modifier.fillMaxSize(),
                    color = LilacDeep,
                    trackColor = InkBlack.copy(alpha = 0.15f),
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
                Estado.FINALIZADO -> Box(
                    Modifier.fillMaxSize().clip(CircleShape).background(KiniroNeonGreen).border(2.dp, InkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** Boceto 3: barra de % + grid de capítulos + Editar/Eliminar */
@Composable
private fun DetalleObra(
    item: ObraConCapitulos,
    categoria: Categoria,
    onCapitulo: (Capitulo) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val obra = item.obra
    Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 6.dp)) {
        HorizontalDivider(color = InkBlack.copy(alpha = 0.15f))
        Spacer(Modifier.height(10.dp))

        if (obra.totalCapitulos > 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { item.progreso },
                    modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)),
                    color = LilacDeep,
                    trackColor = LilacPastel,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(10.dp))
                Text("${(item.progreso * 100).roundToInt()}%", fontWeight = FontWeight.Bold, color = InkBlack)
            }
            Spacer(Modifier.height(10.dp))
            Text("Toca un número para marcarlo o desmarcarlo", fontSize = 11.sp, color = InkBlack.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            GridCapitulos(item.capitulos.sortedBy { it.numero }, categoria, onCapitulo)
            Spacer(Modifier.height(10.dp))
        }

        if (obra.plataforma.isNotBlank()) DatoInfo(Icons.Filled.PlayCircleOutline, "Plataforma: ${obra.plataforma}")
        obra.fechaInicio?.let { DatoInfo(Icons.Filled.CalendarMonth, "Empezaste el ${formatearFechaUtc(it)}") }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Editar", color = InkBlack, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onEliminar) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = RojoBorrar, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar", color = RojoBorrar, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DatoInfo(icono: ImageVector, texto: String) {
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = LilacDeep, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 13.sp, color = InkBlack)
    }
}

/** Grid en bloques de 60 para que obras largas (One Piece 😅) no traben la pantalla */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridCapitulos(capitulos: List<Capitulo>, categoria: Categoria, onCapitulo: (Capitulo) -> Unit) {
    val tamBloque = 60
    val bloques = capitulos.chunked(tamBloque)
    if (bloques.isEmpty()) return

    // Abre directo en el bloque donde te quedaste
    val bloqueInicial = remember { (capitulos.indexOfFirst { !it.visto }.takeIf { it >= 0 } ?: 0) / tamBloque }
    var bloque by rememberSaveable { mutableIntStateOf(bloqueInicial) }
    val actual = bloque.coerceIn(0, bloques.lastIndex)

    if (bloques.size > 1) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { bloque = actual - 1 }, enabled = actual > 0) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bloque anterior")
            }
            Text("${bloques[actual].first().numero} – ${bloques[actual].last().numero}", fontWeight = FontWeight.Bold, color = InkBlack)
            IconButton(onClick = { bloque = actual + 1 }, enabled = actual < bloques.lastIndex) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bloque siguiente")
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        bloques[actual].forEach { cap -> CeldaCapitulo(cap, categoria) { onCapitulo(cap) } }
    }
}

@Composable
private fun CeldaCapitulo(capitulo: Capitulo, categoria: Categoria, onClick: () -> Unit) {
    val forma = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(forma)
            .background(if (capitulo.visto) KiniroSoftGreen else Color.White)
            .border(1.5.dp, InkBlack, forma)
            .toggleable(value = capitulo.visto, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = "${categoria.unidadSingular} ${capitulo.numero}" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${capitulo.numero}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (capitulo.visto) InkBlack.copy(alpha = 0.55f) else InkBlack,
            textDecoration = if (capitulo.visto) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}