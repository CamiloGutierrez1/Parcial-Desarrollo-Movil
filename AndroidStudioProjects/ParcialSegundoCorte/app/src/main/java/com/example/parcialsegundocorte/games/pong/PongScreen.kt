package com.example.parcialsegundocorte.games.pong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

@Composable
fun PongScreen(navController: NavController) {
    var canvasW by remember { mutableStateOf(0f) }
    var canvasH by remember { mutableStateOf(0f) }

    val ballRadius = 22f
    val paddleW = 200f
    val paddleH = 18f
    val playerPaddleMargin = 80f
    val aiPaddleMargin = 80f

    var ballX by remember { mutableStateOf(0f) }
    var ballY by remember { mutableStateOf(0f) }
    var ballDx by remember { mutableStateOf(0f) }
    var ballDy by remember { mutableStateOf(0f) }
    var playerX by remember { mutableStateOf(0f) }
    var aiX by remember { mutableStateOf(0f) }
    var playerScore by remember { mutableStateOf(0) }
    var aiScore by remember { mutableStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf("") }
    val winScore = 7

    fun initBall(direction: Float = 1f) {
        ballX = canvasW / 2
        ballY = canvasH / 2
        val angle = Random.nextFloat() * 0.6f - 0.3f
        val speed = 12f
        ballDx = speed * angle
        ballDy = speed * direction
    }

    fun resetGame() {
        playerScore = 0
        aiScore = 0
        gameOver = false
        winner = ""
        if (canvasW > 0 && canvasH > 0) {
            playerX = canvasW / 2 - paddleW / 2
            aiX = canvasW / 2 - paddleW / 2
            initBall()
        }
    }

    LaunchedEffect(canvasW, canvasH) {
        if (canvasW > 0 && canvasH > 0 && !gameStarted) {
            gameStarted = true
            playerX = canvasW / 2 - paddleW / 2
            aiX = canvasW / 2 - paddleW / 2
            initBall()
        }
    }

    LaunchedEffect(gameStarted, gameOver) {
        if (!gameStarted || gameOver) return@LaunchedEffect
        while (!gameOver) {
            delay(16L)
            if (canvasW == 0f || canvasH == 0f) continue

            ballX += ballDx
            ballY += ballDy

            // Bounce walls left/right
            if (ballX - ballRadius < 0) {
                ballX = ballRadius
                ballDx = abs(ballDx)
            }
            if (ballX + ballRadius > canvasW) {
                ballX = canvasW - ballRadius
                ballDx = -abs(ballDx)
            }

            // AI paddle follows ball (limited speed)
            val aiCenter = aiX + paddleW / 2
            val aiSpeed = 7f
            aiX += ((ballX - aiCenter) * 0.06f).coerceIn(-aiSpeed, aiSpeed)
            aiX = aiX.coerceIn(0f, canvasW - paddleW)

            // Player paddle collision (bottom)
            val playerPaddleY = canvasH - playerPaddleMargin
            if (ballDy > 0 &&
                ballY + ballRadius >= playerPaddleY &&
                ballY - ballRadius < playerPaddleY + paddleH &&
                ballX >= playerX - 5 && ballX <= playerX + paddleW + 5
            ) {
                val hitPos = (ballX - playerX) / paddleW - 0.5f
                ballDx = hitPos * 18f
                ballDy = -(abs(ballDy) + 0.3f).coerceAtMost(22f)
                ballY = playerPaddleY - ballRadius - 1
            }

            // AI paddle collision (top)
            val aiPaddleY = aiPaddleMargin
            if (ballDy < 0 &&
                ballY - ballRadius <= aiPaddleY + paddleH &&
                ballY + ballRadius > aiPaddleY &&
                ballX >= aiX - 5 && ballX <= aiX + paddleW + 5
            ) {
                val hitPos = (ballX - aiX) / paddleW - 0.5f
                ballDx = hitPos * 18f
                ballDy = (abs(ballDy) + 0.3f).coerceAtMost(22f)
                ballY = aiPaddleY + paddleH + ballRadius + 1
            }

            // Ball exits top -> player scores
            if (ballY - ballRadius < 0) {
                playerScore++
                if (playerScore >= winScore) {
                    gameOver = true
                    winner = "¡Ganaste!"
                } else {
                    initBall(1f)
                }
            }

            // Ball exits bottom -> AI scores
            if (ballY + ballRadius > canvasH) {
                aiScore++
                if (aiScore >= winScore) {
                    gameOver = true
                    winner = "La IA gana"
                } else {
                    initBall(-1f)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("← Salir", color = Color(0xFFE94560))
            }
            Text("🏓 PONG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Primero en $winScore pts", color = Color(0xFFAAAAAA), fontSize = 12.sp)
        }

        // Score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("IA", color = Color(0xFFFF6B6B), fontSize = 14.sp)
                Text("$aiScore", color = Color(0xFFFF6B6B), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Text("VS", color = Color(0xFF555555), fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TÚ", color = Color(0xFF6FCF97), fontSize = 14.sp)
                Text("$playerScore", color = Color(0xFF6FCF97), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        canvasW = it.width.toFloat()
                        canvasH = it.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            playerX = (playerX + dragAmount).coerceIn(0f, canvasW - paddleW)
                            change.consume()
                        }
                    }
            ) {
                drawRect(Color(0xFF0D0D1A)) // background

                // Center line dashed
                val dashCount = 15
                val dashH = size.height / (dashCount * 2)
                for (i in 0 until dashCount) {
                    drawRect(
                        color = Color(0xFF333355),
                        topLeft = Offset(size.width / 2 - 2f, i * dashH * 2),
                        size = Size(4f, dashH)
                    )
                }

                if (canvasW > 0 && canvasH > 0) {
                    // AI paddle (top)
                    drawRoundRect(
                        color = Color(0xFFFF6B6B),
                        topLeft = Offset(aiX, aiPaddleMargin.toFloat()),
                        size = Size(paddleW, paddleH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(paddleH / 2)
                    )

                    // Player paddle (bottom)
                    drawRoundRect(
                        color = Color(0xFF6FCF97),
                        topLeft = Offset(playerX, canvasH - playerPaddleMargin),
                        size = Size(paddleW, paddleH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(paddleH / 2)
                    )

                    // Ball glow effect
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = ballRadius * 2.5f,
                        center = Offset(ballX, ballY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = ballRadius,
                        center = Offset(ballX, ballY)
                    )
                }
            }

            // Game Over overlay
            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (winner == "¡Ganaste!") "🏆" else "😔",
                                fontSize = 56.sp
                            )
                            Text(winner, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Marcador: $playerScore - $aiScore",
                                color = Color(0xFFAAAAAA),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { resetGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Jugar de nuevo", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text("Volver al menú", color = Color(0xFFAAAAAA))
                            }
                        }
                    }
                }
            }
        }

        // Instructions
        Text(
            text = "Arrastra horizontalmente para mover tu paleta (verde)",
            color = Color(0xFF555555),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}
