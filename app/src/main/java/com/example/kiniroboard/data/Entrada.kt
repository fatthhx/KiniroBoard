package com.example.kiniroboard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entradas",
    foreignKeys = [
        ForeignKey(
            entity = Coleccion::class,
            parentColumns = ["id"],
            childColumns = ["coleccionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["coleccionId"])]
)
data class Entrada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val coleccionId: Int,
    val titulo: String,
    val notaDetalle: String,
    val fecha: String,
    val progreso: String,
    val calificacion: Double
)