package com.example.parcialsegundocorte.games.tetris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

// ===== CONSTANTS =====
private const val BOARD_W = 10
private const val BOARD_H = 20

// ===== PIECES =====
private val TETROMINOES = listOf(
    arrayOf(intArrayOf(1, 1, 1, 1)),                                      // I
    arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),                           // O
    arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),                    // T
    arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),                    // S
    arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),                    // Z
    arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),                    // J
    arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1))                     // L
)

private val PIECE_COLORS = listOf(
    Color(0xFF00FFFF),  // I - cyan
    Color(0xFFFFFF00),  // O - yellow
    Color(0xFFAA00FF),  // T - purple
    Color(0xFF00FF00),  // S - green
    Color(0xFFFF0000),  // Z - red
    Color(0xFF0000FF),  // J - blue
    Color(0xFFFF8800)   // L - orange
)

// ===== ROTATION =====
private fun rotate90(piece: Array<IntArray>): Array<IntArray> {
    val rows = piece.size
    val cols = piece[0].size
    return Array(cols) { c -> IntArray(rows) { r -> piece[rows - 1 - r][c] } }
}

// ===== GAME STATE =====
data class TetrisState(
    val board: Array<IntArray> = Array(BOARD_H) { IntArray(BOARD_W) },
    val currentPiece: Array<IntArray> = TETROMINOES[0],
    val currentColorIdx: Int = 0,
    val pieceX: Int = 3,
    val pieceY: Int = 0,
    val nextPiece: Array<IntArray> = TETROMINOES[1],
    val nextColorIdx: Int = 1,
    val score: Int = 0,
    val lines: Int = 0,
    val level: Int = 1,
    val isOver: Boolean = false
) {
    override fun equals(other: Any?) = false
    override fun hashCode() = System.identityHashCode(this)
}

private fun canPlace(board: Array<IntArray>, piece: Array<IntArray>, px: Int, py: Int): Boolean {
    for (r in piece.indices) {
        for (c in piece[r].indices) {
            if (piece[r][c] == 0) continue
            val bx = px + c; val by = py + r
            if (bx < 0 || bx >= BOARD_W || by >= BOARD_H) return false
            if (by >= 0 && board[by][bx] != 0) return false
        }
    }
    return true
}

private fun mergePiece(state: TetrisState): Array<IntArray> {
    val newBoard = state.board.map { it.clone() }.toTypedArray()
    for (r in state.currentPiece.indices) {
        for (c in state.currentPiece[r].indices) {
            if (state.currentPiece[r][c] != 0) {
                val bx = state.pieceX + c; val by = state.pieceY + r
                if (by in 0 until BOARD_H && bx in 0 until BOARD_W)
                    newBoard[by][bx] = state.currentColorIdx + 1
            }
        }
    }
    return newBoard
}

private fun clearLines(board: Array<IntArray>): Pair<Array<IntArray>, Int> {
    val filtered = board.filter { row -> row.any { it == 0 } }.toMutableList()
    val cleared = BOARD_H - filtered.size
    repeat(cleared) { filtered.add(0, IntArray(BOARD_W)) }
    return filtered.toTypedArray() to cleared
}

private fun calcScore(lines: Int, level: Int) = when (lines) {
    1 -> 100 * level
    2 -> 300 * level
    3 -> 500 * level
    4 -> 800 * level
    else -> 0
}

private fun ghostY(state: TetrisState): Int {
    var gy = state.pieceY
    while (canPlace(state.board, state.currentPiece, state.pieceX, gy + 1)) gy++
    return gy
}

private fun newPiece(): Pair<Array<IntArray>, Int> {
    val idx = Random.nextInt(TETROMINOES.size)
    return TETROMINOES[idx] to idx
}

@Composable
fun TetrisScreen(navController: NavController) {
    var state by remember {
        val (np, nc) = newPiece()
        val (cp, cc) = newPiece()
        mutableStateOf(
            TetrisState(
                currentPiece = cp, currentColorIdx = cc,
                nextPiece = np, nextColorIdx = nc,
                pieceX = (BOARD_W - cp[0].size) / 2
            )
        )
    }
    var isPaused by remember { mutableStateOf(false) }

    fun resetGame() {
        val (cp, cc) = newPiece()
        val (np, nc) = newPiece()
        state = TetrisState(
            currentPiece = cp, currentColorIdx = cc,
            nextPiece = np, nextColorIdx = nc,
            pieceX = (BOARD_W - cp[0].size) / 2
        )
        isPaused = false
    }

    fun moveLeft() {
        if (state.isOver || isPaused) return
        if (canPlace(state.board, state.currentPiece, state.pieceX - 1, state.pieceY))
            state = state.copy(pieceX = state.pieceX - 1)
    }

    fun moveRight() {
        if (state.isOver || isPaused) return
        if (canPlace(state.board, state.currentPiece, state.pieceX + 1, state.pieceY))
            state = state.copy(pieceX = state.pieceX + 1)
    }

    fun rotatePiece() {
        if (state.isOver || isPaused) return
        val rotated = rotate90(state.currentPiece)
        // Wall kick: try center, then offset ±1, ±2
        val kicks = listOf(0, -1, 1, -2, 2)
        for (kick in kicks) {
            if (canPlace(state.board, rotated, state.pieceX + kick, state.pieceY)) {
                state = state.copy(currentPiece = rotated, pieceX = state.pieceX + kick)
                break
            }
        }
    }

    fun softDrop() {
        if (state.isOver || isPaused) return
        if (canPlace(state.board, state.currentPiece, state.pieceX, state.pieceY + 1)) {
            state = state.copy(pieceY = state.pieceY + 1, score = state.score + 1)
        }
    }

    fun hardDrop() {
        if (state.isOver || isPaused) return
        var s = state
        var dropped = 0
        while (canPlace(s.board, s.currentPiece, s.pieceX, s.pieceY + 1)) {
            s = s.copy(pieceY = s.pieceY + 1)
            dropped++
        }
        // Lock piece
        val newBoard = mergePiece(s)
        val (clearedBoard, linesCleared) = clearLines(newBoard)
        val newLines = s.lines + linesCleared
        val newLevel = newLines / 10 + 1
        val (np, nc) = newPiece()
        val startX = (BOARD_W - s.nextPiece[0].size) / 2
        val isGameOver = !canPlace(clearedBoard, s.nextPiece, startX, 0)
        state = s.copy(
            board = clearedBoard,
            currentPiece = s.nextPiece,
            currentColorIdx = s.nextColorIdx,
            pieceX = startX,
            pieceY = 0,
            nextPiece = np,
            nextColorIdx = nc,
            score = s.score + calcScore(linesCleared, s.level) + dropped * 2,
            lines = newLines,
            level = newLevel,
            isOver = isGameOver
        )
    }

    // Game loop
    LaunchedEffect(state.isOver, isPaused) {
        if (state.isOver || isPaused) return@LaunchedEffect
        while (!state.isOver && !isPaused) {
            val dropInterval = maxOf(100L, 1000L - (state.level - 1) * 90L)
            delay(dropInterval)
            if (state.isOver || isPaused) break
            val s = state
            if (canPlace(s.board, s.currentPiece, s.pieceX, s.pieceY + 1)) {
                state = s.copy(pieceY = s.pieceY + 1)
            } else {
                // Lock piece
                val newBoard = mergePiece(s)
                val (clearedBoard, linesCleared) = clearLines(newBoard)
                val newLines = s.lines + linesCleared
                val newLevel = newLines / 10 + 1
                val (np, nc) = newPiece()
                val startX = (BOARD_W - s.nextPiece[0].size) / 2
                val isGameOver = !canPlace(clearedBoard, s.nextPiece, startX, 0)
                state = s.copy(
                    board = clearedBoard,
                    currentPiece = s.nextPiece,
                    currentColorIdx = s.nextColorIdx,
                    pieceX = startX,
                    pieceY = 0,
                    nextPiece = np,
                    nextColorIdx = nc,
                    score = s.score + calcScore(linesCleared, s.level),
                    lines = newLines,
                    level = newLevel,
                    isOver = isGameOver
                )
            }
        }
    }

    // ===== UI =====
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("← Salir", color = Color(0xFFE94560), fontSize = 13.sp)
            }
            Text("🟦 TETRIS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = { isPaused = !isPaused }) {
                Text(if (isPaused) "▶ Seguir" else "⏸ Pausa", color = Color(0xFFE94560), fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Board canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val currentGhostY = remember(state) { ghostY(state) }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / BOARD_W
                    val cellH = size.height / BOARD_H

                    // Background grid
                    drawRect(Color(0xFF0D1117))
                    for (r in 0 until BOARD_H) {
                        for (c in 0 until BOARD_W) {
                            drawRoundRect(
                                color = Color(0xFF161B22),
                                topLeft = Offset(c * cellW + 1, r * cellH + 1),
                                size = Size(cellW - 2, cellH - 2),
                                cornerRadius = CornerRadius(3f)
                            )
                        }
                    }

                    // Draw placed blocks
                    for (r in 0 until BOARD_H) {
                        for (c in 0 until BOARD_W) {
                            val colorIdx = state.board[r][c]
                            if (colorIdx > 0) {
                                drawBlock(c, r, PIECE_COLORS[colorIdx - 1], cellW, cellH)
                            }
                        }
                    }

                    // Ghost piece
                    if (!state.isOver) {
                        for (r in state.currentPiece.indices) {
                            for (c in state.currentPiece[r].indices) {
                                if (state.currentPiece[r][c] != 0) {
                                    val bx = state.pieceX + c
                                    val by = currentGhostY + r
                                    if (by in 0 until BOARD_H && bx in 0 until BOARD_W) {
                                        drawRoundRect(
                                            color = PIECE_COLORS[state.currentColorIdx].copy(alpha = 0.25f),
                                            topLeft = Offset(bx * cellW + 2, by * cellH + 2),
                                            size = Size(cellW - 4, cellH - 4),
                                            cornerRadius = CornerRadius(4f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Current piece
                    if (!state.isOver) {
                        for (r in state.currentPiece.indices) {
                            for (c in state.currentPiece[r].indices) {
                                if (state.currentPiece[r][c] != 0) {
                                    val bx = state.pieceX + c
                                    val by = state.pieceY + r
                                    if (by in 0 until BOARD_H && bx in 0 until BOARD_W) {
                                        drawBlock(bx, by, PIECE_COLORS[state.currentColorIdx], cellW, cellH)
                                    }
                                }
                            }
                        }
                    }
                }

                // Game over overlay
                if (state.isOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("😵", fontSize = 40.sp)
                                Text("Game Over", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Puntos: ${state.score}", color = Color(0xFFE94560), fontWeight = FontWeight.Bold)
                                Text("Líneas: ${state.lines}", color = Color(0xFFAAAAAA))
                                Button(
                                    onClick = { resetGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("Nuevo juego", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }

                // Pause overlay
                if (isPaused && !state.isOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏸ PAUSA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Side panel
            Column(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SideCard(label = "SCORE") {
                    Text("${state.score}", color = Color(0xFFE94560), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                SideCard(label = "NIVEL") {
                    Text("${state.level}", color = Color(0xFF6FCF97), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                SideCard(label = "LÍNEAS") {
                    Text("${state.lines}", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                SideCard(label = "SIGUIENTE") {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val cw = size.width / 4
                        val ch = size.height / 4
                        val piece = state.nextPiece
                        val offsetX = (4 - piece[0].size) / 2f
                        val offsetY = (4 - piece.size) / 2f
                        for (r in piece.indices) {
                            for (c in piece[r].indices) {
                                if (piece[r][c] != 0) {
                                    drawBlock(
                                        (offsetX + c).toInt(), (offsetY + r).toInt(),
                                        PIECE_COLORS[state.nextColorIdx], cw, ch
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1117))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                GameButton(label = "↺ Rotar", modifier = Modifier.weight(1f)) { rotatePiece() }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameButton(label = "◀", modifier = Modifier.weight(1f)) { moveLeft() }
                GameButton(label = "▼ Bajar", modifier = Modifier.weight(1.5f)) { softDrop() }
                GameButton(label = "▶", modifier = Modifier.weight(1f)) { moveRight() }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GameButton(
                    label = "⬇ Hard Drop",
                    modifier = Modifier.fillMaxWidth(0.7f),
                    color = Color(0xFFE94560)
                ) { hardDrop() }
            }
        }
    }
}

private fun DrawScope.drawBlock(cx: Int, cy: Int, color: Color, cellW: Float, cellH: Float) {
    val pad = 2f
    val x = cx * cellW + pad
    val y = cy * cellH + pad
    val w = cellW - pad * 2
    val h = cellH - pad * 2
    // Shadow
    drawRoundRect(
        color = color.copy(alpha = 0.3f),
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f)
    )
    // Main block
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f)
    )
    // Highlight
    drawRoundRect(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(w * 0.5f, h * 0.3f),
        cornerRadius = CornerRadius(2f)
    )
}

@Composable
private fun SideCard(label: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
    ) {
        Column(
            modifier = Modifier.padding(6.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color(0xFF888888), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun GameButton(label: String, modifier: Modifier = Modifier, color: Color = Color(0xFF1F2544), onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
