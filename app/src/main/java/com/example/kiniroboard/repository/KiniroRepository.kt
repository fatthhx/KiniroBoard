package com.example.kiniroboard.repository

import com.example.kiniroboard.data.Coleccion
import com.example.kiniroboard.data.Entrada
import com.example.kiniroboard.data.KiniroDao
import kotlinx.coroutines.flow.Flow

class KiniroRepository(private val dao: KiniroDao) {

    val todasLasColecciones: Flow<List<Coleccion>> = dao.obtenerTodasLasColecciones()

    fun obtenerColeccion(id: Int): Flow<Coleccion?> = dao.obtenerColeccionPorId(id)

    fun obtenerEntradas(coleccionId: Int): Flow<List<Entrada>> =
        dao.obtenerEntradasPorColeccion(coleccionId)

    suspend fun insertarColeccion(coleccion: Coleccion) = dao.insertarColeccion(coleccion)
    suspend fun actualizarColeccion(coleccion: Coleccion) = dao.actualizarColeccion(coleccion)
    suspend fun eliminarColeccion(coleccion: Coleccion) = dao.eliminarColeccion(coleccion)

    suspend fun insertarEntrada(entrada: Entrada) = dao.insertarEntrada(entrada)
    suspend fun actualizarEntrada(entrada: Entrada) = dao.actualizarEntrada(entrada)
    suspend fun eliminarEntrada(entrada: Entrada) = dao.eliminarEntrada(entrada)
}