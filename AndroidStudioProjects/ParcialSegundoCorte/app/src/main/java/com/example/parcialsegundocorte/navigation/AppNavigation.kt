package com.example.parcialsegundocorte.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parcialsegundocorte.auth.ForgotPasswordScreen
import com.example.parcialsegundocorte.auth.LoginScreen
import com.example.parcialsegundocorte.auth.RegisterScreen
import com.example.parcialsegundocorte.games.minesweeper.MinesweeperScreen
import com.example.parcialsegundocorte.games.pong.PongScreen
import com.example.parcialsegundocorte.games.tetris.TetrisScreen
import com.example.parcialsegundocorte.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Pong : Screen("pong")
    object Minesweeper : Screen("minesweeper")
    object Tetris : Screen("tetris")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Pong.route) {
            PongScreen(navController = navController)
        }
        composable(Screen.Minesweeper.route) {
            MinesweeperScreen(navController = navController)
        }
        composable(Screen.Tetris.route) {
            TetrisScreen(navController = navController)
        }
    }
}
