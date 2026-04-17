package com.example.parcialsegundocorte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parcialsegundocorte.navigation.AppNavigation
import com.example.parcialsegundocorte.ui.theme.ParcialSegundoCorteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParcialSegundoCorteTheme {
                AppNavigation()
            }
        }
    }
}
