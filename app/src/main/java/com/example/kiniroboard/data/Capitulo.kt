package com.example.kiniroboard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capitulos",
    foreignKeys = [
        ForeignKey(
            entity = Obra::class,
            parentColumns = ["id"],
            childColumns = ["obraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["obraId"]),
        Index(value = ["obraId", "numero"], unique = true)
    ]
)
data class Capitulo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val obraId: Int,
    val numero: Int,
    val visto: Boolean = false,
    val fechaVisto: Long? = null
)