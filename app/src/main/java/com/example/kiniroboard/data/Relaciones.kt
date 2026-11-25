package com.example.kiniroboard.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kiniroboard.model.Estado

// Una Obra + TODOS sus capítulos, armado automáticamente por Room
data class ObraConCapitulos(
    @Embedded val obra: Obra,
    @Relation(parentColumn = "id", entityColumn = "obraId")
    val capitulos: List<Capitulo>
)

// Datos calculados (no se guardan en la BD, se sacan de los capítulos)
val ObraConCapitulos.vistos: Int get() = capitulos.count { it.visto }

val ObraConCapitulos.progreso: Float
    get() = if (obra.totalCapitulos <= 0) 0f
    else (vistos.toFloat() / obra.totalCapitulos).coerceIn(0f, 1f)

val ObraConCapitulos.estado: Estado
    get() = when {
        vistos == 0 -> Estado.POR_VER
        vistos >= obra.totalCapitulos -> Estado.FINALIZADO
        else -> Estado.EMPEZADO
    }

val ObraConCapitulos.ultimoVisto: Int
    get() = capitulos.filter { it.visto }.maxOfOrNull { it.numero } ?: 0

// Una fila de la pantalla Cronología
data class EventoCronologia(
    val obraId: Int,
    val categoria: String,
    val titulo: String,
    val imagenPath: String?,
    val totalCapitulos: Int,
    val numero: Int,
    val fechaVisto: Long
)

// Cuántas obras hay por categoría (para la pantalla de inicio)
data class ConteoCategoria(
    val categoria: String,
    val total: Int
)