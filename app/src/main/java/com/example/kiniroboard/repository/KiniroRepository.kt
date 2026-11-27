package com.example.kiniroboard.repository

import androidx.room.withTransaction
import com.example.kiniroboard.data.Capitulo
import com.example.kiniroboard.data.ConteoCategoria
import com.example.kiniroboard.data.Cronologia
import com.example.kiniroboard.data.CronologiaConEntradas
import com.example.kiniroboard.data.EntradaCronologia
import com.example.kiniroboard.data.KiniroDatabase
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.data.ObraConCapitulos
import com.example.kiniroboard.model.Categoria
import kotlinx.coroutines.flow.Flow

class KiniroRepository(private val db: KiniroDatabase) {

    private val dao = db.kiniroDao()

    // ======================= OBRAS Y CAPÍTULOS =======================

    fun obras(categoria: Categoria): Flow<List<ObraConCapitulos>> = dao.obrasConCapitulos(categoria.name)

    fun conteos(): Flow<List<ConteoCategoria>> = dao.conteoPorCategoria()

    fun todasLasObras(): Flow<List<Obra>> = dao.obtenerTodasLasObras()

    suspend fun obtenerObra(id: Int): ObraConCapitulos? = dao.obraConCapitulos(id)

    suspend fun guardarObra(obra: Obra) {
        db.withTransaction {
            if (obra.id == 0) {
                val nuevoId = dao.insertarObra(obra).toInt()
                dao.insertarCapitulos((1..obra.totalCapitulos).map { Capitulo(obraId = nuevoId, numero = it) })
            } else {
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

    // ======================= CRONOLOGÍAS =======================

    fun cronologias(): Flow<List<CronologiaConEntradas>> = dao.obtenerCronologias()

    fun cronologia(id: Int): Flow<CronologiaConEntradas?> = dao.obtenerCronologia(id)

    suspend fun obtenerEntradaCronologia(id: Int): EntradaCronologia? = dao.obtenerEntradaCronologia(id)

    suspend fun guardarCronologia(cronologia: Cronologia) {
        if (cronologia.id == 0) dao.insertarCronologia(cronologia) else dao.actualizarCronologia(cronologia)
    }

    suspend fun eliminarCronologia(cronologia: Cronologia) = dao.eliminarCronologia(cronologia)

    /** Las entradas nuevas se agregan AL FINAL; luego el usuario las acomoda con ↑ ↓ */
    suspend fun guardarEntradaCronologia(entrada: EntradaCronologia) {
        db.withTransaction {
            if (entrada.id == 0) {
                val siguiente = dao.maxOrden(entrada.cronologiaId) + 1
                dao.insertarEntradaCronologia(entrada.copy(orden = siguiente))
            } else {
                dao.actualizarEntradaCronologia(entrada)
            }
        }
    }

    suspend fun actualizarEntradasCronologia(entradas: List<EntradaCronologia>) =
        dao.actualizarEntradasCronologia(entradas)

    suspend fun eliminarEntradaCronologia(entrada: EntradaCronologia) = dao.eliminarEntradaCronologia(entrada)

    /** Para "Deshacer": se vuelve a insertar tal cual (con su mismo id y posición) */
    suspend fun restaurarEntradaCronologia(entrada: EntradaCronologia) {
        dao.insertarEntradaCronologia(entrada)
    }
}