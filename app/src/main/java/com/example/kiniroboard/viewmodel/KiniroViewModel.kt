package com.example.kiniroboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiniroboard.data.Coleccion
import com.example.kiniroboard.data.Entrada
import com.example.kiniroboard.repository.KiniroRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KiniroViewModel(private val repository: KiniroRepository) : ViewModel() {

    private val _filtroCategoria = MutableStateFlow("Todos")
    val filtroCategoria: StateFlow<String> = _filtroCategoria.asStateFlow()

    val colecciones: StateFlow<List<Coleccion>> = combine(
        repository.todasLasColecciones,
        _filtroCategoria
    ) { lista, categoria ->
        if (categoria == "Todos") lista
        else lista.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun cambiarFiltro(categoria: String) {
        _filtroCategoria.value = categoria
    }

    fun obtenerColeccionPorId(id: Int): StateFlow<Coleccion?> {
        return repository.obtenerColeccion(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun obtenerEntradasDeColeccion(coleccionId: Int): StateFlow<List<Entrada>> {
        return repository.obtenerEntradas(coleccionId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun guardarColeccion(coleccion: Coleccion) {
        viewModelScope.launch {
            if (coleccion.id == 0) repository.insertarColeccion(coleccion)
            else repository.actualizarColeccion(coleccion)
        }
    }

    fun borrarColeccion(coleccion: Coleccion) {
        viewModelScope.launch {
            repository.eliminarColeccion(coleccion)
        }
    }

    fun guardarEntrada(entrada: Entrada) {
        viewModelScope.launch {
            if (entrada.id == 0) repository.insertarEntrada(entrada)
            else repository.actualizarEntrada(entrada)
        }
    }

    fun borrarEntrada(entrada: Entrada) {
        viewModelScope.launch {
            repository.eliminarEntrada(entrada)
        }
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