package com.example.kiniroboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiniroboard.data.Capitulo
import com.example.kiniroboard.data.Cronologia
import com.example.kiniroboard.data.CronologiaConEntradas
import com.example.kiniroboard.data.EntradaConObra
import com.example.kiniroboard.data.EntradaCronologia
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.data.ObraConCapitulos
import com.example.kiniroboard.data.desdeEfectivo
import com.example.kiniroboard.data.estado
import com.example.kiniroboard.data.hastaEfectivo
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.model.Estado
import com.example.kiniroboard.model.Filtro
import com.example.kiniroboard.repository.KiniroRepository
import com.example.kiniroboard.util.borrarImagen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class KiniroViewModel(private val repository: KiniroRepository) : ViewModel() {

    // ======================= OBRAS Y CAPÍTULOS =======================

    private val _categoria = MutableStateFlow(Categoria.ANIME)
    val categoria: StateFlow<Categoria> = _categoria.asStateFlow()

    private val _filtro = MutableStateFlow(Filtro.TODOS)
    val filtro: StateFlow<Filtro> = _filtro.asStateFlow()

    val conteos: StateFlow<Map<Categoria, Int>> = repository.conteos()
        .map { lista ->
            lista.mapNotNull { conteo ->
                Categoria.entries.firstOrNull { it.name == conteo.categoria }?.let { it to conteo.total }
            }.toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val obras: StateFlow<List<ObraConCapitulos>> = combine(
        _categoria.flatMapLatest { repository.obras(it) },
        _filtro
    ) { lista, filtro ->
        when (filtro) {
            Filtro.PENDIENTES -> lista.filter { it.estado == Estado.POR_VER }
            Filtro.EMPEZADOS -> lista.filter { it.estado == Estado.EMPEZADO }
            Filtro.FINALIZADOS -> lista.filter { it.estado == Estado.FINALIZADO }
            Filtro.TODOS -> lista
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasObras: StateFlow<List<Obra>> = repository.todasLasObras()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarCategoria(categoria: Categoria) {
        if (_categoria.value != categoria) {
            _categoria.value = categoria
            _filtro.value = Filtro.TODOS
        }
    }

    fun cambiarFiltro(filtro: Filtro) { _filtro.value = filtro }

    suspend fun obtenerObra(id: Int): ObraConCapitulos? = repository.obtenerObra(id)

    fun guardarObra(obra: Obra, imagenAnterior: String?) {
        viewModelScope.launch {
            repository.guardarObra(obra)
            if (imagenAnterior != null && imagenAnterior != obra.imagenPath) {
                withContext(Dispatchers.IO) { borrarImagen(imagenAnterior) }
            }
        }
    }

    fun eliminarObra(obra: Obra) {
        viewModelScope.launch {
            repository.eliminarObra(obra) // capítulos y entradas de cronología se van en cascada
            obra.imagenPath?.let { withContext(Dispatchers.IO) { borrarImagen(it) } }
        }
    }

    fun alternarCapitulo(capitulo: Capitulo) {
        viewModelScope.launch {
            val ahoraVisto = !capitulo.visto
            repository.actualizarCapitulo(
                capitulo.copy(visto = ahoraVisto, fechaVisto = if (ahoraVisto) System.currentTimeMillis() else null)
            )
        }
    }

    fun marcarTodo(obra: ObraConCapitulos, completa: Boolean) {
        marcarCapitulos(obra.capitulos, completa)
    }

    fun restaurarCapitulos(capitulos: List<Capitulo>) {
        viewModelScope.launch { repository.actualizarCapitulos(capitulos) }
    }

    private fun marcarCapitulos(capitulos: List<Capitulo>, completa: Boolean) {
        val ahora = System.currentTimeMillis()
        viewModelScope.launch {
            repository.actualizarCapitulos(
                capitulos.map {
                    when {
                        completa && !it.visto -> it.copy(visto = true, fechaVisto = ahora)
                        completa -> it
                        else -> it.copy(visto = false, fechaVisto = null)
                    }
                }
            )
        }
    }

    // ======================= CRONOLOGÍAS =======================

    val cronologias: StateFlow<List<CronologiaConEntradas>> = repository.cronologias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cronología abierta en pantalla (se crea UNA vez; cambia con flatMapLatest)
    private val _cronologiaId = MutableStateFlow<Int?>(null)

    val cronologiaActual: StateFlow<CronologiaConEntradas?> = _cronologiaId
        .flatMapLatest { id -> if (id == null) flowOf(null) else repository.cronologia(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun abrirCronologia(id: Int) { _cronologiaId.value = id }

    fun guardarCronologia(cronologia: Cronologia) {
        viewModelScope.launch { repository.guardarCronologia(cronologia) }
    }

    fun eliminarCronologia(cronologia: Cronologia) {
        viewModelScope.launch { repository.eliminarCronologia(cronologia) }
    }

    suspend fun obtenerEntradaCronologia(id: Int): EntradaCronologia? = repository.obtenerEntradaCronologia(id)

    fun guardarEntradaCronologia(entrada: EntradaCronologia) {
        viewModelScope.launch { repository.guardarEntradaCronologia(entrada) }
    }

    fun eliminarEntradaCronologia(entrada: EntradaCronologia) {
        viewModelScope.launch { repository.eliminarEntradaCronologia(entrada) }
    }

    fun restaurarEntradaCronologia(entrada: EntradaCronologia) {
        viewModelScope.launch { repository.restaurarEntradaCronologia(entrada) }
    }

    /**
     * Sube (-1) o baja (+1) una entrada en el orden de la historia.
     * Solo intercambia su "orden" con la vecina.
     */
    fun moverEntrada(ordenadas: List<EntradaConObra>, indice: Int, direccion: Int) {
        val destino = indice + direccion
        if (indice !in ordenadas.indices || destino !in ordenadas.indices) return
        val a = ordenadas[indice].entrada
        val b = ordenadas[destino].entrada
        viewModelScope.launch {
            repository.actualizarEntradasCronologia(listOf(a.copy(orden = b.orden), b.copy(orden = a.orden)))
        }
    }

    /** Marca (o desmarca) como vistos solo los capítulos del rango de esa entrada. */
    fun marcarRango(entrada: EntradaConObra, completa: Boolean) {
        val rango = entrada.desdeEfectivo..entrada.hastaEfectivo
        marcarCapitulos(entrada.obra.capitulos.filter { it.numero in rango }, completa)
    }
}

class KiniroViewModelFactory(private val repository: KiniroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KiniroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KiniroViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
