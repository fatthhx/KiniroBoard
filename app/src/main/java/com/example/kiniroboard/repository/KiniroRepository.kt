package com.example.kiniroboard.repository

import androidx.room.withTransaction
import com.example.kiniroboard.data.Capitulo
import com.example.kiniroboard.data.Coleccion
import com.example.kiniroboard.data.ConteoCategoria
import com.example.kiniroboard.data.Entrada
import com.example.kiniroboard.data.EventoCronologia
import com.example.kiniroboard.data.KiniroDatabase
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.data.ObraConCapitulos
import com.example.kiniroboard.model.Categoria
import kotlinx.coroutines.flow.Flow

class KiniroRepository(private val db: KiniroDatabase) {

    private val dao = db.kiniroDao()

    val todasLasColecciones: Flow<List<Coleccion>> = dao.obtenerTodasLasColecciones()
    fun obtenerColeccion(id: Int): Flow<Coleccion?> = dao.obtenerColeccionPorId(id)
    fun obtenerEntradas(coleccionId: Int): Flow<List<Entrada>> = dao.obtenerEntradasPorColeccion(coleccionId)
    suspend fun insertarColeccion(coleccion: Coleccion) = dao.insertarColeccion(coleccion)
    suspend fun actualizarColeccion(coleccion: Coleccion) = dao.actualizarColeccion(coleccion)
    suspend fun eliminarColeccion(coleccion: Coleccion) = dao.eliminarColeccion(coleccion)
    suspend fun insertarEntrada(entrada: Entrada) = dao.insertarEntrada(entrada)
    suspend fun actualizarEntrada(entrada: Entrada) = dao.actualizarEntrada(entrada)
    suspend fun eliminarEntrada(entrada: Entrada) = dao.eliminarEntrada(entrada)

    // ================== NUEVO ==================
    fun obras(categoria: Categoria): Flow<List<ObraConCapitulos>> = dao.obrasConCapitulos(categoria.name)

    fun conteos(): Flow<List<ConteoCategoria>> = dao.conteoPorCategoria()

    fun cronologia(categoria: Categoria): Flow<List<EventoCronologia>> = dao.cronologia(categoria.name)

    suspend fun obtenerObra(id: Int): ObraConCapitulos? = dao.obraConCapitulos(id)

    // Crea o actualiza una obra y mantiene sus capítulos sincronizados
    suspend fun guardarObra(obra: Obra) {
        db.withTransaction {
            if (obra.id == 0) {
                // NUEVA: se inserta y se crean sus capítulos 1..N
                val nuevoId = dao.insertarObra(obra).toInt()
                dao.insertarCapitulos((1..obra.totalCapitulos).map { Capitulo(obraId = nuevoId, numero = it) })
            } else {
                // EDICIÓN: se actualiza y se ajusta la cantidad de capítulos
                dao.actualizarObra(obra)
                dao.eliminarCapitulosMayoresA(obra.id, obra.totalCapitulos)
                val existentes = dao.maxNumero(obra.id) ?: 0
                if (obra.totalCapitulos > existentes) {
                    dao.insertarCapitulos(
                        ((existentes + 1)..obra.totalCapitulos).map { Capitulo(obraId = obra.id, numero = it) }
                    )
                }
            }
        }
    }

    suspend fun eliminarObra(obra: Obra) = dao.eliminarObra(obra)
    suspend fun actualizarCapitulo(capitulo: Capitulo) = dao.actualizarCapitulo(capitulo)
    suspend fun actualizarCapitulos(capitulos: List<Capitulo>) = dao.actualizarCapitulos(capitulos)
}