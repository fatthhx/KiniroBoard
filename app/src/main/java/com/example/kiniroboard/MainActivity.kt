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
import com.example.kiniroboard.data.Obra
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.repository.KiniroRepository
import com.example.kiniroboard.ui.PantallaCategoria
import com.example.kiniroboard.ui.PantallaDetalleColeccion
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

                NavHost(
                    navController = navController,
                    startDestination = "inicio"
                ) {
                    // ① INICIO: las píldoras de categorías
                    composable("inicio") {
                        PantallaInicio(
                            viewModel = viewModel,
                            onAbrirCategoria = { categoria ->
                                navController.navigate("categoria/${categoria.name}")   // ← CAMBIÓ
                            }
                        )
                    }

                    // ② CATEGORÍA: la pantalla nueva de la Fase 5   ← NUEVO BLOQUE
                    composable(
                        route = "categoria/{cat}",
                        arguments = listOf(navArgument("cat") { type = NavType.StringType })
                    ) { entrada ->
                        val categoria = Categoria.entries.firstOrNull {
                            it.name == entrada.arguments?.getString("cat")
                        } ?: Categoria.ANIME

                        PantallaCategoria(
                            categoria = categoria,
                            viewModel = viewModel,
                            onVolver = { navController.popBackStack() },
                            onNuevo = {
                                // TEMPORAL (en la Fase 6 se cambia por el formulario):
                                // crea una obra de prueba para poder probar la pantalla
                                viewModel.guardarObra(
                                    Obra(
                                        categoria = categoria.name,
                                        titulo = "Prueba ${(100..999).random()}",
                                        totalCapitulos = if (categoria == Categoria.PELICULAS) 1 else 24,
                                        plataforma = "Crunchyroll"
                                    ),
                                    imagenAnterior = null
                                )
                            },
                            onEditar = { /* TEMPORAL: Fase 6 */ },
                            onCronologia = { /* TEMPORAL: Fase 7 */ }
                        )
                    }

                    // ③ VIEJAS: ya nadie llega aquí; se borran en la Fase 7
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