package com.example.kiniroboard.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "obras", indices = [Index(value = ["categoria"])])
data class Obra(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoria: String,
    val titulo: String,
    val totalCapitulos: Int,
    val plataforma: String = "",
    val imagenPath: String? = null,
    val fechaInicio: Long? = null,
    val fechaRegistro: Long = System.currentTimeMillis()
)