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
import com.example.kiniroboard.model.categoriaPorNombre
import com.example.kiniroboard.repository.KiniroRepository
import com.example.kiniroboard.ui.PantallaCategoria
import com.example.kiniroboard.ui.PantallaFormulario
import com.example.kiniroboard.ui.PantallaInicio
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

                NavHost(navController = navController, startDestination = "inicio") {

                    // ① INICIO
                    composable("inicio") {
                        PantallaInicio(
                            viewModel = viewModel,
                            onAbrirCategoria = { navController.navigate("categoria/${it.name}") }
                        )
                    }

                    // ② CATEGORÍA
                    composable(
                        route = "categoria/{cat}",
                        arguments = listOf(navArgument("cat") { type = NavType.StringType })
                    ) { entrada ->
                        val categoria = categoriaPorNombre(entrada.arguments?.getString("cat"))
                        PantallaCategoria(
                            categoria = categoria,
                            viewModel = viewModel,
                            onVolver = { navController.popBackStack() },
                            onNuevo = { navController.navigate("formulario/${categoria.name}") },
                            onEditar = { id -> navController.navigate("formulario/${categoria.name}?obraId=$id") },
                            onCronologia = { /* Fase 8B */ }
                        )
                    }

                    // ③ FORMULARIO (crear y editar)
                    composable(
                        route = "formulario/{cat}?obraId={obraId}",
                        arguments = listOf(
                            navArgument("cat") { type = NavType.StringType },
                            navArgument("obraId") { type = NavType.IntType; defaultValue = -1 }
                        )
                    ) { entrada ->
                        PantallaFormulario(
                            categoria = categoriaPorNombre(entrada.arguments?.getString("cat")),
                            obraId = entrada.arguments?.getInt("obraId")?.takeIf { it > 0 },
                            viewModel = viewModel,
                            onCerrar = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}