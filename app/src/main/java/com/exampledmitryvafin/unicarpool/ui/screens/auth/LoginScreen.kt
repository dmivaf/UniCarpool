package com.exampledmitryvafin.unicarpool.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exampledmitryvafin.unicarpool.R
import com.exampledmitryvafin.unicarpool.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.unicarpoollogo),
            contentDescription = "Logo UniCarpool",
            modifier = Modifier
                .size(240.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Inicia sesión para compartir viajes",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it
                            emailError = null},
            label = { Text("Correo electrónico") },
            placeholder = { Text("nombre@universidad.es") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(emailError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it
                            passwordError = null},
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                emailError = null
                passwordError = null
                var hasError = false

                if (email.isBlank()) {
                    emailError = "El correo electrónico es obligatorio"
                    hasError = true
                }

                if (password.isBlank()) {
                    passwordError = "La contraseña es obligatoria"
                    hasError = true
                }

                if (!hasError) {
                    authViewModel.login(email, password) { success, message ->
                        if (!success) {
                            if (message.contains("contraseña", ignoreCase = true))
                                passwordError = message
                            else
                                emailError = message
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { showRecoveryDialog = true },
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿No tienes cuenta? ")
            TextButton(onClick = onNavigateToRegister) { Text("Regístrate") }
        }
    }

    if (showRecoveryDialog) {
        var step by remember { mutableStateOf(1) } // 1: pedir email, 2: mostrar pregunta y respuesta, 3: mostrar contraseña
        var recoveryEmail by remember { mutableStateOf("") }
        var securityQuestion by remember { mutableStateOf("") }
        var securityAnswer by remember { mutableStateOf("") }
        var recoveredPassword by remember { mutableStateOf<String?>(null) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showRecoveryDialog = false
                step = 1
                recoveryEmail = ""
                securityQuestion = ""
                securityAnswer = ""
                recoveredPassword = null
                errorMsg = null
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    when (step) {
                        1 -> {
                            OutlinedTextField(
                                value = recoveryEmail,
                                onValueChange = { recoveryEmail = it; errorMsg = null },
                                label = { Text("Correo electrónico") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = errorMsg != null,
                                supportingText = { if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error) }
                            )
                        }
                        2 -> {
                            Text("Pregunta de seguridad:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(securityQuestion, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = securityAnswer,
                                onValueChange = { securityAnswer = it; errorMsg = null },
                                label = { Text("Respuesta") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = errorMsg != null,
                                supportingText = { if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error) }
                            )
                        }
                        3 -> {
                            Text("Tu contraseña es:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(recoveredPassword ?: "", fontSize = 16.sp, color = Color.Green)
                        }
                    }
                }
            },
            confirmButton = {
                when (step) {
                    1 -> {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val question = authViewModel.getSecurityQuestion(recoveryEmail)
                                    if (question != null) {
                                        securityQuestion = question
                                        step = 2
                                        errorMsg = null
                                    } else {
                                        errorMsg = "Correo no registrado"
                                    }
                                }
                            }
                        ) {
                            Text("Verificar correo")
                        }
                    }
                    2 -> {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val password = authViewModel.recoverPassword(recoveryEmail, securityAnswer)
                                    if (password != null) {
                                        recoveredPassword = password
                                        step = 3
                                        errorMsg = null
                                    } else {
                                        errorMsg = "Respuesta incorrecta"
                                    }
                                }
                            }
                        ) {
                            Text("Verificar respuesta")
                        }
                    }
                    3 -> {
                        Button(onClick = { showRecoveryDialog = false }) {
                            Text("Cerrar")
                        }
                    }
                }
            },
            dismissButton = {
                if (step != 3) {
                    TextButton(onClick = { showRecoveryDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}