package com.example.kiniroboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kiniroboard.data.KiniroDatabase
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.repository.KiniroRepository
import com.example.kiniroboard.ui.PantallaCategoria
import com.example.kiniroboard.ui.PantallaDetalleColeccion
import com.example.kiniroboard.ui.PantallaFormulario
import com.example.kiniroboard.ui.PantallaInicio
import com.example.kiniroboard.ui.PantallaTableroPrincipal
import com.example.kiniroboard.ui.theme.KiniroBoardTheme
import com.example.kiniroboard.viewmodel.KiniroViewModel
import com.example.kiniroboard.viewmodel.KiniroViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KiniroDatabase.getDatabase(applicationContext)
        val repository = KiniroRepository(database)
        val viewModelFactory = KiniroViewModelFactory(repository)

        setContent {
            KiniroBoardTheme {
                val viewModel: KiniroViewModel = viewModel(factory = viewModelFactory)
                val navController = rememberNavController()

                // Convierte el texto de la ruta ("MANGAS") en la categoría del enum
                fun categoriaDe(valor: String?): Categoria =
                    Categoria.entries.firstOrNull { it.name == valor } ?: Categoria.ANIME

                NavHost(
                    navController = navController,
                    startDestination = "inicio"
                ) {
                    // ① INICIO
                    composable("inicio") {
                        PantallaInicio(
                            viewModel = viewModel,
                            onAbrirCategoria = { categoria ->
                                navController.navigate("categoria/${categoria.name}")
                            }
                        )
                    }

                    // ② CATEGORÍA
                    composable(
                        route = "categoria/{cat}",
                        arguments = listOf(navArgument("cat") { type = NavType.StringType })
                    ) { entrada ->
                        val categoria = categoriaDe(entrada.arguments?.getString("cat"))
                        PantallaCategoria(
                            categoria = categoria,
                            viewModel = viewModel,
                            onVolver = { navController.popBackStack() },
                            onNuevo = { navController.navigate("formulario/${categoria.name}") },              // ← CAMBIÓ
                            onEditar = { id -> navController.navigate("formulario/${categoria.name}?obraId=$id") }, // ← CAMBIÓ
                            onCronologia = { /* TEMPORAL: Fase 7 */ }
                        )
                    }

                    // ③ FORMULARIO (crear y editar)   ← NUEVO
                    composable(
                        route = "formulario/{cat}?obraId={obraId}",
                        arguments = listOf(
                            navArgument("cat") { type = NavType.StringType },
                            navArgument("obraId") { type = NavType.IntType; defaultValue = -1 }
                        )
                    ) { entrada ->
                        PantallaFormulario(
                            categoria = categoriaDe(entrada.arguments?.getString("cat")),
                            obraId = entrada.arguments?.getInt("obraId")?.takeIf { it > 0 },
                            viewModel = viewModel,
                            onCerrar = { navController.popBackStack() }
                        )
                    }

                    // ④ VIEJAS: se borran en la Fase 7
                    composable("tablero_principal") {
                        PantallaTableroPrincipal(
                            viewModel = viewModel,
                            onSeleccionarColeccion = { coleccionId ->
                                navController.navigate("detalle_coleccion/$coleccionId")
                            }
                        )
                    }

                    composable(
                        route = "detalle_coleccion/{coleccionId}",
                        arguments = listOf(navArgument("coleccionId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val coleccionId = backStackEntry.arguments?.getInt("coleccionId") ?: 0
                        PantallaDetalleColeccion(
                            coleccionId = coleccionId,
                            viewModel = viewModel,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}