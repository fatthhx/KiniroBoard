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
import com.example.kiniroboard.repository.KiniroRepository
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
                    // NUEVA: pantalla de inicio con las píldoras
                    composable("inicio") {
                        PantallaInicio(
                            viewModel = viewModel,
                            onAbrirCategoria = {
                                // TEMPORAL: en la Fase 5 irá a la pantalla de la categoría
                                navController.navigate("tablero_principal")
                            }
                        )
                    }

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

