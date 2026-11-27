package com.example.kiniroboard.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kiniroboard.model.Estado

// ======================= OBRAS =======================

data class ObraConCapitulos(
    @Embedded val obra: Obra,
    @Relation(parentColumn = "id", entityColumn = "obraId")
    val capitulos: List<Capitulo>
)

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

data class ConteoCategoria(
    val categoria: String,
    val total: Int
)

// ======================= CRONOLOGÍAS =======================

/** Una entrada + su obra completa (con capítulos, para calcular el progreso). */
data class EntradaConObra(
    @Embedded val entrada: EntradaCronologia,
    @Relation(entity = Obra::class, parentColumn = "obraId", entityColumn = "id")
    val obra: ObraConCapitulos
)

/** Cronología → entradas → obra → capítulos (relación anidada) */
data class CronologiaConEntradas(
    @Embedded val cronologia: Cronologia,
    @Relation(entity = EntradaCronologia::class, parentColumn = "id", entityColumn = "cronologiaId")
    val entradas: List<EntradaConObra>
)

// Rango real de capítulos que abarca la entrada
val EntradaConObra.desdeEfectivo: Int get() = (entrada.desde ?: 1).coerceAtLeast(1)

val EntradaConObra.hastaEfectivo: Int
    get() = (entrada.hasta ?: obra.obra.totalCapitulos).coerceAtMost(obra.obra.totalCapitulos)

val EntradaConObra.totalRango: Int get() = (hastaEfectivo - desdeEfectivo + 1).coerceAtLeast(0)

val EntradaConObra.vistosRango: Int
    get() = obra.capitulos.count { it.visto && it.numero in desdeEfectivo..hastaEfectivo }

val EntradaConObra.progresoRango: Float
    get() = if (totalRango == 0) 0f else vistosRango.toFloat() / totalRango

val EntradaConObra.terminada: Boolean get() = totalRango > 0 && vistosRango >= totalRango

val EntradaConObra.esCompleta: Boolean get() = entrada.desde == null && entrada.hasta == null

/** Entradas en el orden de la historia */
val CronologiaConEntradas.ordenadas: List<EntradaConObra> get() = entradas.sortedBy { it.entrada.orden }

val CronologiaConEntradas.terminadas: Int get() = entradas.count { it.terminada }