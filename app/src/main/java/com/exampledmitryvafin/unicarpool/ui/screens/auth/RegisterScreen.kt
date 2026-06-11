package com.exampledmitryvafin.unicarpool.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exampledmitryvafin.unicarpool.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Regístrate con tu correo universitario",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo universitario") },
            placeholder = { Text("nombre@alumno.ucm.es") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ... después de confirmPassword
        OutlinedTextField(
            value = securityQuestion,
            onValueChange = { securityQuestion = it },
            label = { Text("Pregunta de seguridad") },
            placeholder = { Text("Ej: ¿Nombre de tu primera mascota?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = securityAnswer,
            onValueChange = { securityAnswer = it },
            label = { Text("Respuesta de seguridad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )


        Spacer(modifier = Modifier.height(24.dp))

        // Botón de registro
        Button(
            onClick = {
                // Validación local antes de llamar al ViewModel
                if (password != confirmPassword) {
                    showError = true
                    errorMessage = "Las contraseñas no coinciden"
                    return@Button
                }

                authViewModel.registerUser(name, email, password, securityQuestion, securityAnswer) { success, message ->
                    if (success) {
                        onRegisterSuccess()
                    } else {
                        showError = true
                        errorMessage = message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace a login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿Ya tienes cuenta?")
            TextButton(onClick = onNavigateToLogin) {
                Text("Inicia sesión")
            }
        }
    }

    // Diálogo de error
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
