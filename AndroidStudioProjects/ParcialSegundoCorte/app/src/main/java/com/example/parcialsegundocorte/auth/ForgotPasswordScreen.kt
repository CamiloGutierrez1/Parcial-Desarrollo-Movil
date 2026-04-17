package com.example.parcialsegundocorte.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🔑", fontSize = 64.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recuperar Contraseña",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE94560)
            )
            Text(
                text = "Te enviaremos un enlace para restablecer\ntu contraseña",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; message = "" },
                        label = { Text("Correo electrónico", color = Color(0xFFAAAAAA)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color(0xFFE94560)
                        )
                    )

                    if (message.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) Color(0xFF1B4332) else Color(0xFF4A1B1B)
                            )
                        ) {
                            Text(
                                text = message,
                                color = if (isSuccess) Color(0xFF6FCF97) else Color(0xFFFF6B6B),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                message = "Ingresa tu correo electrónico"
                                isSuccess = false
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                message = "Correo electrónico inválido"
                                isSuccess = false
                                return@Button
                            }
                            isLoading = true
                            message = ""
                            auth.sendPasswordResetEmail(email.trim())
                                .addOnSuccessListener {
                                    isLoading = false
                                    isSuccess = true
                                    message = "✓ Correo enviado. Revisa tu bandeja de entrada."
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    isSuccess = false
                                    message = when {
                                        e.message?.contains("user") == true -> "No existe una cuenta con ese correo"
                                        e.message?.contains("network") == true -> "Error de conexión a internet"
                                        else -> "Error al enviar el correo. Intenta de nuevo."
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ENVIAR CORREO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("← Volver al inicio de sesión", color = Color(0xFFE94560))
            }
        }
    }
}
