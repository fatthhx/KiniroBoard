package com.example.kiniroboard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una línea de tiempo creada por el usuario: "Marvel", "Naruto", "Star Wars"... */
@Entity(tableName = "cronologias")
data class Cronologia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)

/**
 * Un paso dentro de una cronología: apunta a una Obra (de cualquier categoría)
 * y opcionalmente a un rango de sus capítulos ("Naruto Shippuden, ep. 1 a 53").
 */
@Entity(
    tableName = "entradas_cronologia",
    foreignKeys = [
        ForeignKey(
            entity = Cronologia::class,
            parentColumns = ["id"],
            childColumns = ["cronologiaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Obra::class,
            parentColumns = ["id"],
            childColumns = ["obraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cronologiaId"]), Index(value = ["obraId"])]
)
data class EntradaCronologia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cronologiaId: Int,
    val obraId: Int,
    val desde: Int? = null,        // null = desde el capítulo 1
    val hasta: Int? = null,        // null = hasta el último
    val orden: Int = 0,            // posición en la HISTORIA (la decide el usuario)
    val anioEstreno: Int? = null,  // opcional, para el "orden de estreno"
    val nota: String = ""          // "Aún no animado", "Relleno"...
)