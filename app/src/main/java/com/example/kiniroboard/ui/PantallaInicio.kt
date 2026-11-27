package com.example.kiniroboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiniroboard.model.Categoria
import com.example.kiniroboard.ui.theme.*
import com.example.kiniroboard.viewmodel.KiniroViewModel

@Composable
fun PantallaInicio(
    viewModel: KiniroViewModel,
    onAbrirCategoria: (Categoria) -> Unit
) {
    val conteos by viewModel.conteos.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(PaperCream)) {
        // Capa 1: garabatos de fondo
        FondoGarabatos(Modifier.fillMaxSize())

        // Capa 2: el contenido encima
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Marca
            Surface(color = InkBlack, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(KiniroNeonGreen))
                    Spacer(Modifier.width(8.dp))
                    Text("KINIRO BOARD", color = KiniroNeonGreen, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.5.sp)
                }
            }
            Text("¿Qué vas a registrar hoy?", color = InkBlack, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            Spacer(Modifier.height(8.dp))

            // Una píldora por cada categoría del enum
            Categoria.entries.forEach { categoria ->
                PildoraCategoria(
                    categoria = categoria,
                    total = conteos[categoria] ?: 0,
                    onClick = { onAbrirCategoria(categoria) }
                )
            }
        }
    }
}

@Composable
private fun PildoraCategoria(categoria: Categoria, total: Int, onClick: () -> Unit) {
    val textoTotal = if (total == 1) "1 obra" else "$total obras"
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = categoria.color,
        border = BorderStroke(2.dp, InkBlack),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics { contentDescription = "${categoria.nombre}, $textoTotal" }
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(categoria.icono, contentDescription = null, tint = InkBlack)
            Spacer(Modifier.width(14.dp))
            Text(categoria.nombre, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = InkBlack)
            Text(textoTotal, fontSize = 12.sp, color = InkBlack.copy(alpha = 0.7f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkBlack)
        }
    }
}