package com.example.kiniroboard.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.kiniroboard.ui.theme.KiniroSoftGreen
import com.example.kiniroboard.ui.theme.LilacPastel
import com.example.kiniroboard.ui.theme.PinkPastel
import com.example.kiniroboard.ui.theme.YellowPastel

enum class Categoria(
    val nombre: String,
    val femenino: Boolean,
    val pendiente: String,
    val unidad: String,
    val unidadSingular: String,
    val unidadCorta: String,
    val color: Color,
    val icono: ImageVector
) {
    ANIME("Anime", false, "Por ver", "Episodios", "Episodio", "Ep.", KiniroSoftGreen, Icons.Filled.LiveTv),
    SERIES("Series", true, "Por ver", "Episodios", "Episodio", "Ep.", LilacPastel, Icons.Filled.Tv),
    PELICULAS("Películas", true, "Por ver", "Partes", "Parte", "Parte", PinkPastel, Icons.Filled.Movie),
    LIBROS("Libros", false, "Por leer", "Capítulos", "Capítulo", "Cap.", YellowPastel, Icons.AutoMirrored.Filled.MenuBook),
    COMICS("Cómics", false, "Por leer", "Capítulos", "Capítulo", "Cap.", LilacPastel, Icons.Filled.AutoStories),
    MANGAS("Mangas", false, "Por leer", "Capítulos", "Capítulo", "Cap.", KiniroSoftGreen, Icons.Filled.CollectionsBookmark);

    fun genero(masculino: String, femenino: String) = if (this.femenino) femenino else masculino
}

enum class Estado { POR_VER, EMPEZADO, FINALIZADO }

enum class Filtro { PENDIENTES, EMPEZADOS, FINALIZADOS, TODOS }

// Texto de cada pestaña según la categoría ("Por ver"/"Por leer", "Empezados"/"Empezadas"...)
fun Filtro.etiqueta(categoria: Categoria): String = when (this) {
    Filtro.PENDIENTES -> categoria.pendiente
    Filtro.EMPEZADOS -> categoria.genero("Empezados", "Empezadas")
    Filtro.FINALIZADOS -> categoria.genero("Finalizados", "Finalizadas")
    Filtro.TODOS -> categoria.genero("Todos", "Todas")
}

/** Convierte el texto guardado en la BD ("MANGAS") en la categoría del enum */
fun categoriaPorNombre(valor: String?): Categoria =
    Categoria.entries.firstOrNull { it.name == valor } ?: Categoria.ANIME