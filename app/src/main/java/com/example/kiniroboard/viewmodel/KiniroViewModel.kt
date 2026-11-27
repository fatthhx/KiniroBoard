package com.example.kiniroboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiniroboard.data.Capitulo
import com.example.kiniroboard.data.Coleccion
import com.example.kiniroboard.data.Entrada
import com.example.kiniroboard.data.EventoCronologia
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.data.ObraConCapitulos
import com.example.kiniroboard.data.estado
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

    private val _filtroCategoria = MutableStateFlow("Todos")
    val filtroCategoria: StateFlow<String> = _filtroCategoria.asStateFlow()

    val colecciones: StateFlow<List<Coleccion>> = combine(
        repository.todasLasColecciones, _filtroCategoria
    ) { lista, categoria ->
        if (categoria == "Todos") lista else lista.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarFiltro(categoria: String) { _filtroCategoria.value = categoria }

    fun obtenerColeccionPorId(id: Int): StateFlow<Coleccion?> =
        repository.obtenerColeccion(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun obtenerEntradasDeColeccion(coleccionId: Int): StateFlow<List<Entrada>> =
        repository.obtenerEntradas(coleccionId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarColeccion(coleccion: Coleccion) = viewModelScope.launch {
        if (coleccion.id == 0) repository.insertarColeccion(coleccion) else repository.actualizarColeccion(coleccion)
    }
    fun borrarColeccion(coleccion: Coleccion) = viewModelScope.launch { repository.eliminarColeccion(coleccion) }
    fun guardarEntrada(entrada: Entrada) = viewModelScope.launch {
        if (entrada.id == 0) repository.insertarEntrada(entrada) else repository.actualizarEntrada(entrada)
    }
    fun borrarEntrada(entrada: Entrada) = viewModelScope.launch { repository.eliminarEntrada(entrada) }

    // ================== NUEVO ==================

    // Categoría abierta y pestaña seleccionada
    private val _categoria = MutableStateFlow(Categoria.ANIME)
    val categoria: StateFlow<Categoria> = _categoria.asStateFlow()

    private val _filtro = MutableStateFlow(Filtro.TODOS)
    val filtro: StateFlow<Filtro> = _filtro.asStateFlow()

    // Cuántas obras hay por categoría (pantalla de inicio)
    val conteos: StateFlow<Map<Categoria, Int>> = repository.conteos()
        .map { lista ->
            lista.mapNotNull { conteo ->
                Categoria.entries.firstOrNull { it.name == conteo.categoria }?.let { it to conteo.total }
            }.toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Obras de la categoría actual, filtradas por la pestaña
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

    val cronologia: StateFlow<List<EventoCronologia>> = _categoria
        .flatMapLatest { repository.cronologia(it) }
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
            // Si cambió la portada, se borra el archivo viejo
            if (imagenAnterior != null && imagenAnterior != obra.imagenPath) {
                withContext(Dispatchers.IO) { borrarImagen(imagenAnterior) }
            }
        }
    }

    fun eliminarObra(obra: Obra) {
        viewModelScope.launch {
            repository.eliminarObra(obra) // los capítulos se van en cascada
            obra.imagenPath?.let { withContext(Dispatchers.IO) { borrarImagen(it) } }
        }
    }

    // Toque en un número del grid
    fun alternarCapitulo(capitulo: Capitulo) {
        viewModelScope.launch {
            val ahoraVisto = !capitulo.visto
            repository.actualizarCapitulo(
                capitulo.copy(visto = ahoraVisto, fechaVisto = if (ahoraVisto) System.currentTimeMillis() else null)
            )
        }
    }

    // Toque en el círculo: terminar todo o reiniciar
    fun marcarTodo(obra: ObraConCapitulos, completa: Boolean) {
        val ahora = System.currentTimeMillis()
        viewModelScope.launch {
            repository.actualizarCapitulos(
                obra.capitulos.map {
                    when {
                        completa && !it.visto -> it.copy(visto = true, fechaVisto = ahora)
                        completa -> it
                        else -> it.copy(visto = false, fechaVisto = null)
                    }
                }
            )
        }
    }

    // Botón "Deshacer"
    fun restaurarCapitulos(capitulos: List<Capitulo>) {
        viewModelScope.launch { repository.actualizarCapitulos(capitulos) }
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