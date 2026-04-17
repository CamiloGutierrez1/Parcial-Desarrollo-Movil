package com.example.parcialsegundocorte.games.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Cell(
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMines: Int = 0
)

private const val ROWS = 9
private const val COLS = 9
private const val MINES = 10

private fun createEmptyBoard(): List<List<Cell>> = List(ROWS) { List(COLS) { Cell() } }

private fun placeMines(board: List<List<Cell>>, excludeR: Int, excludeC: Int): List<List<Cell>> {
    val mutable = board.map { it.toMutableList() }.toMutableList()
    var placed = 0
    while (placed < MINES) {
        val r = Random.nextInt(ROWS)
        val c = Random.nextInt(COLS)
        if (!mutable[r][c].isMine && !(r == excludeR && c == excludeC)) {
            mutable[r][c] = mutable[r][c].copy(isMine = true)
            placed++
        }
    }
    // Calculate adjacents
    for (r in 0 until ROWS) {
        for (c in 0 until COLS) {
            if (!mutable[r][c].isMine) {
                var count = 0
                for (dr in -1..1) for (dc in -1..1) {
                    val nr = r + dr; val nc = c + dc
                    if (nr in 0 until ROWS && nc in 0 until COLS && mutable[nr][nc].isMine) count++
                }
                mutable[r][c] = mutable[r][c].copy(adjacentMines = count)
            }
        }
    }
    return mutable.map { it.toList() }
}

private fun revealCell(board: List<List<Cell>>, row: Int, col: Int): List<List<Cell>> {
    val mutable = board.map { it.toMutableList() }.toMutableList()
    fun flood(r: Int, c: Int) {
        if (r !in 0 until ROWS || c !in 0 until COLS) return
        val cell = mutable[r][c]
        if (cell.isRevealed || cell.isFlagged || cell.isMine) return
        mutable[r][c] = cell.copy(isRevealed = true)
        if (cell.adjacentMines == 0) {
            for (dr in -1..1) for (dc in -1..1) {
                if (dr != 0 || dc != 0) flood(r + dr, c + dc)
            }
        }
    }
    if (!mutable[row][col].isFlagged) flood(row, col)
    return mutable.map { it.toList() }
}

private fun revealAllMines(board: List<List<Cell>>): List<List<Cell>> =
    board.mapIndexed { _, row ->
        row.map { cell -> if (cell.isMine) cell.copy(isRevealed = true) else cell }
    }

private fun checkWin(board: List<List<Cell>>): Boolean =
    board.all { row -> row.all { c -> (c.isMine && !c.isRevealed) || (!c.isMine && c.isRevealed) } }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MinesweeperScreen(navController: NavController) {
    var board by remember { mutableStateOf(createEmptyBoard()) }
    var firstMove by remember { mutableStateOf(true) }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    var flagCount by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning, gameOver, gameWon) {
        if (timerRunning && !gameOver && !gameWon) {
            while (timerRunning && !gameOver && !gameWon) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    fun resetGame() {
        board = createEmptyBoard()
        firstMove = true
        gameOver = false
        gameWon = false
        flagCount = 0
        elapsedSeconds = 0
        timerRunning = false
    }

    fun onCellClick(r: Int, c: Int) {
        if (gameOver || gameWon || board[r][c].isRevealed || board[r][c].isFlagged) return
        var current = board
        if (firstMove) {
            current = placeMines(current, r, c)
            firstMove = false
            timerRunning = true
        }
        if (current[r][c].isMine) {
            board = revealAllMines(current)
            gameOver = true
            timerRunning = false
            return
        }
        current = revealCell(current, r, c)
        board = current
        if (checkWin(current)) {
            gameWon = true
            timerRunning = false
        }
    }

    fun onCellLongClick(r: Int, c: Int) {
        if (gameOver || gameWon || board[r][c].isRevealed) return
        val cell = board[r][c]
        val delta = if (cell.isFlagged) -1 else 1
        board = board.mapIndexed { ri, row ->
            row.mapIndexed { ci, c2 ->
                if (ri == r && ci == c) c2.copy(isFlagged = !c2.isFlagged) else c2
            }
        }
        flagCount += delta
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
            Text("💣 BUSCAMINAS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = { resetGame() }) {
                Text("🔄 Nuevo", color = Color(0xFFE94560))
            }
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBox(label = "Minas", value = "${MINES - flagCount}", color = Color(0xFFFF6B6B))
            StatBox(label = "Banderas", value = "$flagCount", color = Color(0xFFF2C94C))
            StatBox(label = "Tiempo", value = "${elapsedSeconds}s", color = Color(0xFF6FCF97))
        }

        // Status message
        val statusMsg = when {
            gameWon -> "🏆 ¡Ganaste! Campo despejado"
            gameOver -> "💥 ¡Boom! Pisaste una mina"
            else -> "Toca para revelar • Mantén presionado para bandear"
        }
        Text(
            text = statusMsg,
            color = when {
                gameWon -> Color(0xFF6FCF97)
                gameOver -> Color(0xFFFF6B6B)
                else -> Color(0xFFAAAAAA)
            },
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Game board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                for (r in 0 until ROWS) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (c in 0 until COLS) {
                            val cell = board[r][c]
                            CellView(
                                cell = cell,
                                onClick = { onCellClick(r, c) },
                                onLongClick = { onCellLongClick(r, c) }
                            )
                        }
                    }
                }
            }

            // Game over overlay
            if (gameOver || gameWon) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(if (gameWon) "🏆" else "💥", fontSize = 56.sp)
                            Text(
                                if (gameWon) "¡Ganaste!" else "¡Boom!",
                                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                            Text(
                                if (gameWon) "Tiempo: ${elapsedSeconds}s" else "Más suerte la próxima",
                                color = Color(0xFFAAAAAA)
                            )
                            Button(
                                onClick = { resetGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Jugar de nuevo", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CellView(cell: Cell, onClick: () -> Unit, onLongClick: () -> Unit) {
    val cellSize = 34.dp
    val bgColor = when {
        !cell.isRevealed -> Color(0xFF2C3E6B)
        cell.isMine -> Color(0xFF8B0000)
        else -> Color(0xFF1A2340)
    }
    val borderColor = when {
        !cell.isRevealed -> Color(0xFF4A6FA5)
        else -> Color(0xFF222D50)
    }
    val numColor = when (cell.adjacentMines) {
        1 -> Color(0xFF4FC3F7)
        2 -> Color(0xFF81C784)
        3 -> Color(0xFFEF5350)
        4 -> Color(0xFF7E57C2)
        5 -> Color(0xFFFF8A65)
        6 -> Color(0xFF4DD0E1)
        7 -> Color(0xFFF06292)
        8 -> Color(0xFFBDBDBD)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            !cell.isRevealed && cell.isFlagged -> Text("🚩", fontSize = 16.sp)
            !cell.isRevealed -> Unit
            cell.isMine -> Text("💣", fontSize = 16.sp)
            cell.adjacentMines > 0 -> Text(
                "${cell.adjacentMines}",
                color = numColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFFAAAAAA), fontSize = 12.sp)
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
