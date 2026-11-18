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
}