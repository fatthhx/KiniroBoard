package com.example.kiniroboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colecciones")
data class Coleccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val categoria: String,
    val descripcion: String,
    val temaColor: String
)