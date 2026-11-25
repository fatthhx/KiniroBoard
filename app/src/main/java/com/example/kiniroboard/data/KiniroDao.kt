package com.example.kiniroboard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KiniroDao {

    @Query("SELECT * FROM colecciones ORDER BY id DESC")
    fun obtenerTodasLasColecciones(): Flow<List<Coleccion>>

    @Query("SELECT * FROM colecciones WHERE id = :id")
    fun obtenerColeccionPorId(id: Int): Flow<Coleccion?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarColeccion(coleccion: Coleccion)

    @Update
    suspend fun actualizarColeccion(coleccion: Coleccion)

    @Delete
    suspend fun eliminarColeccion(coleccion: Coleccion)

    @Query("SELECT * FROM entradas WHERE coleccionId = :coleccionId ORDER BY id DESC")
    fun obtenerEntradasPorColeccion(coleccionId: Int): Flow<List<Entrada>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEntrada(entrada: Entrada)

    @Update
    suspend fun actualizarEntrada(entrada: Entrada)

    @Delete
    suspend fun eliminarEntrada(entrada: Entrada)

    // ================== NUEVO: Obras y Capítulos ==================

    @Transaction
    @Query("SELECT * FROM obras WHERE categoria = :categoria ORDER BY fechaRegistro DESC")
    fun obrasConCapitulos(categoria: String): Flow<List<ObraConCapitulos>>

    @Transaction
    @Query("SELECT * FROM obras WHERE id = :id")
    suspend fun obraConCapitulos(id: Int): ObraConCapitulos?

    @Query("SELECT categoria, COUNT(*) AS total FROM obras GROUP BY categoria")
    fun conteoPorCategoria(): Flow<List<ConteoCategoria>>

    @Query(
        """
        SELECT o.id AS obraId, o.categoria AS categoria, o.titulo AS titulo,
               o.imagenPath AS imagenPath, o.totalCapitulos AS totalCapitulos,
               c.numero AS numero, c.fechaVisto AS fechaVisto
        FROM capitulos c
        INNER JOIN obras o ON o.id = c.obraId
        WHERE o.categoria = :categoria AND c.visto = 1 AND c.fechaVisto IS NOT NULL
        ORDER BY c.fechaVisto DESC
        """
    )
    fun cronologia(categoria: String): Flow<List<EventoCronologia>>

    @Query("SELECT MAX(numero) FROM capitulos WHERE obraId = :obraId")
    suspend fun maxNumero(obraId: Int): Int?

    @Insert
    suspend fun insertarObra(obra: Obra): Long

    @Insert
    suspend fun insertarCapitulos(capitulos: List<Capitulo>)

    @Update
    suspend fun actualizarObra(obra: Obra)

    @Update
    suspend fun actualizarCapitulo(capitulo: Capitulo)

    @Update
    suspend fun actualizarCapitulos(capitulos: List<Capitulo>)

    @Delete
    suspend fun eliminarObra(obra: Obra)

    @Query("DELETE FROM capitulos WHERE obraId = :obraId AND numero > :total")
    suspend fun eliminarCapitulosMayoresA(obraId: Int, total: Int)

}