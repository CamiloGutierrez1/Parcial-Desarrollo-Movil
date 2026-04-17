package com.example.parcialsegundocorte.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcialsegundocorte.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("Jugador") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("fullName")?.split(" ")?.firstOrNull() ?: "Jugador"
                }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión", color = Color.White) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?", color = Color(0xFFAAAAAA)) },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    showLogoutDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }) { Text("Cerrar sesión", color = Color(0xFFE94560)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color(0xFFAAAAAA))
                }
            },
            containerColor = Color(0xFF1F2544)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "¡Hola, $userName! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = auth.currentUser?.email ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
                IconButton(onClick = { showLogoutDialog = true }) {
                    Text("🚪", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "🎮 Selecciona un juego",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE94560),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            GameCard(
                emoji = "🏓",
                title = "Pong",
                description = "El clásico juego de pelota.\nVence a la IA en este duelo épico.",
                gradient = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                onClick = { navController.navigate(Screen.Pong.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GameCard(
                emoji = "💣",
                title = "Buscaminas",
                description = "Encuentra todas las minas\nsin detonar ninguna. ¡Suerte!",
                gradient = listOf(Color(0xFFEB5757), Color(0xFF000000)),
                onClick = { navController.navigate(Screen.Minesweeper.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GameCard(
                emoji = "🟦",
                title = "Tetris",
                description = "El legendario puzzle de bloques.\nNiveles, puntuación y piezas fantasma.",
                gradient = listOf(Color(0xFF4776E6), Color(0xFF8E54E9)),
                onClick = { navController.navigate(Screen.Tetris.route) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "GameZone v1.0",
                fontSize = 12.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GameCard(
    emoji: String,
    title: String,
    description: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradient))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = emoji, fontSize = 48.sp)
                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
            Text(
                text = "▶",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
