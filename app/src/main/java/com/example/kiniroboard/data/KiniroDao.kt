package com.example.kiniroboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KiniroDao {

    // ======================= OBRAS Y CAPÍTULOS =======================

    // ---------- LEER ----------
    @Transaction
    @Query("SELECT * FROM obras WHERE categoria = :categoria ORDER BY fechaRegistro DESC")
    fun obrasConCapitulos(categoria: String): Flow<List<ObraConCapitulos>>

    @Transaction
    @Query("SELECT * FROM obras WHERE id = :id")
    suspend fun obraConCapitulos(id: Int): ObraConCapitulos?

    @Query("SELECT categoria, COUNT(*) AS total FROM obras GROUP BY categoria")
    fun conteoPorCategoria(): Flow<List<ConteoCategoria>>

    @Query("SELECT MAX(numero) FROM capitulos WHERE obraId = :obraId")
    suspend fun maxNumero(obraId: Int): Int?

    /** Todas las obras de todas las categorías (para elegir qué agregar a una cronología) */
    @Query("SELECT * FROM obras ORDER BY titulo COLLATE NOCASE")
    fun obtenerTodasLasObras(): Flow<List<Obra>>

    // ---------- CREAR ----------
    @Insert
    suspend fun insertarObra(obra: Obra): Long

    @Insert
    suspend fun insertarCapitulos(capitulos: List<Capitulo>)

    // ---------- ACTUALIZAR ----------
    @Update
    suspend fun actualizarObra(obra: Obra)

    @Update
    suspend fun actualizarCapitulo(capitulo: Capitulo)

    @Update
    suspend fun actualizarCapitulos(capitulos: List<Capitulo>)

    // ---------- BORRAR ----------
    @Delete
    suspend fun eliminarObra(obra: Obra)   // capítulos y entradas de cronología se van en CASCADA

    @Query("DELETE FROM capitulos WHERE obraId = :obraId AND numero > :total")
    suspend fun eliminarCapitulosMayoresA(obraId: Int, total: Int)

    // ======================= CRONOLOGÍAS =======================

    // ---------- LEER ----------
    @Transaction
    @Query("SELECT * FROM cronologias ORDER BY nombre COLLATE NOCASE")
    fun obtenerCronologias(): Flow<List<CronologiaConEntradas>>

    @Transaction
    @Query("SELECT * FROM cronologias WHERE id = :id")
    fun obtenerCronologia(id: Int): Flow<CronologiaConEntradas?>

    @Query("SELECT * FROM entradas_cronologia WHERE id = :id")
    suspend fun obtenerEntradaCronologia(id: Int): EntradaCronologia?

    @Query("SELECT COALESCE(MAX(orden), -1) FROM entradas_cronologia WHERE cronologiaId = :cronologiaId")
    suspend fun maxOrden(cronologiaId: Int): Int

    // ---------- CREAR ----------
    @Insert
    suspend fun insertarCronologia(cronologia: Cronologia): Long

    @Insert
    suspend fun insertarEntradaCronologia(entrada: EntradaCronologia): Long

    // ---------- ACTUALIZAR ----------
    @Update
    suspend fun actualizarCronologia(cronologia: Cronologia)

    @Update
    suspend fun actualizarEntradaCronologia(entrada: EntradaCronologia)

    @Update
    suspend fun actualizarEntradasCronologia(entradas: List<EntradaCronologia>)

    // ---------- BORRAR ----------
    @Delete
    suspend fun eliminarCronologia(cronologia: Cronologia)   // sus entradas se van en CASCADA

    @Delete
    suspend fun eliminarEntradaCronologia(entrada: EntradaCronologia)
}