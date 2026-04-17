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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parcialsegundocorte.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("Cédula de Ciudadanía") }
    var documentNumber by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedDocType by remember { mutableStateOf(false) }

    val documentTypes = listOf("Cédula de Ciudadanía", "Tarjeta de Identidad")
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        yearRange = 1920..Calendar.getInstance().get(Calendar.YEAR)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("Aceptar", color = Color(0xFFE94560)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Color(0xFFAAAAAA))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun validate(): String {
        if (fullName.trim().length < 10) return "El nombre debe tener mínimo 10 caracteres"
        val docNum = documentNumber.trim()
        if (documentType == "Cédula de Ciudadanía") {
            if (docNum.length < 6 || !docNum.all { it.isDigit() })
                return "La cédula debe tener mínimo 6 dígitos"
        } else {
            if (docNum.length < 10 || !docNum.all { it.isDigit() })
                return "La tarjeta de identidad debe tener mínimo 10 dígitos"
        }
        if (birthDateMillis == null) return "Selecciona tu fecha de nacimiento"
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        if (birthDateMillis!! >= today) return "La fecha de nacimiento no puede ser hoy ni una fecha futura"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
            return "Ingresa un correo electrónico válido"
        if (password.length < 6) return "La contraseña debe tener mínimo 6 caracteres"
        if (password != confirmPassword) return "Las contraseñas no coinciden"
        return ""
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
            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE94560)
            )
            Text(
                text = "Completa tus datos para registrarte",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2544))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nombre completo
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMessage = "" },
                        label = { Text("Nombre completo (mín. 10 caracteres)", color = Color(0xFFAAAAAA)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("${fullName.length} caracteres", color = Color(0xFF888888)) },
                        colors = fieldColors()
                    )

                    // Tipo de documento
                    ExposedDropdownMenuBox(
                        expanded = expandedDocType,
                        onExpandedChange = { expandedDocType = !expandedDocType }
                    ) {
                        OutlinedTextField(
                            value = documentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de documento", color = Color(0xFFAAAAAA)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDocType) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDocType,
                            onDismissRequest = { expandedDocType = false },
                            modifier = Modifier.background(Color(0xFF1F2544))
                        ) {
                            documentTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = Color.White) },
                                    onClick = {
                                        documentType = type
                                        documentNumber = ""
                                        expandedDocType = false
                                    }
                                )
                            }
                        }
                    }

                    // Número de documento
                    val docHint = if (documentType == "Cédula de Ciudadanía")
                        "Número de cédula (mín. 6 dígitos)"
                    else
                        "Número de tarjeta (mín. 10 dígitos)"

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = { if (it.all { c -> c.isDigit() }) { documentNumber = it; errorMessage = "" } },
                        label = { Text(docHint, color = Color(0xFFAAAAAA)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors()
                    )

                    // Fecha de nacimiento
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
                    ) {
                        Text(
                            text = if (birthDateMillis != null)
                                "Fecha de nacimiento: ${dateFormatter.format(Date(birthDateMillis!!))}"
                            else
                                "Seleccionar fecha de nacimiento",
                            color = if (birthDateMillis != null) Color.White else Color(0xFFAAAAAA)
                        )
                    }

                    // Correo
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = { Text("Correo electrónico", color = Color(0xFFAAAAAA)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors()
                    )

                    // Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Contraseña (mín. 6 caracteres)", color = Color(0xFFAAAAAA)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Ocultar" else "Ver", color = Color(0xFFE94560), fontSize = 12.sp)
                            }
                        },
                        colors = fieldColors()
                    )

                    // Confirmar contraseña
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        label = { Text("Confirmar contraseña", color = Color(0xFFAAAAAA)) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Text(if (confirmPasswordVisible) "Ocultar" else "Ver", color = Color(0xFFE94560), fontSize = 12.sp)
                            }
                        },
                        colors = fieldColors()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            val validationError = validate()
                            if (validationError.isNotEmpty()) {
                                errorMessage = validationError
                                return@Button
                            }
                            isLoading = true
                            errorMessage = ""
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener { result ->
                                    val uid = result.user?.uid ?: ""
                                    val userData = hashMapOf(
                                        "fullName" to fullName.trim(),
                                        "documentType" to documentType,
                                        "documentNumber" to documentNumber.trim(),
                                        "birthDate" to dateFormatter.format(Date(birthDateMillis!!)),
                                        "email" to email.trim(),
                                        "createdAt" to com.google.firebase.Timestamp.now()
                                    )
                                    db.collection("users").document(uid).set(userData)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = when {
                                        e.message?.contains("email") == true -> "El correo ya está registrado"
                                        e.message?.contains("network") == true -> "Error de conexión a internet"
                                        else -> "Error al crear la cuenta: ${e.message}"
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
                            Text("REGISTRARSE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", color = Color(0xFFAAAAAA))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Inicia sesión", color = Color(0xFFE94560), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFE94560),
    unfocusedBorderColor = Color(0xFF444444),
    cursorColor = Color(0xFFE94560)
)
