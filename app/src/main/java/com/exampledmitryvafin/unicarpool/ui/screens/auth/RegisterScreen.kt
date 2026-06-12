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
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var questionError by remember { mutableStateOf<String?>(null) }
    var answerError by remember { mutableStateOf<String?>(null) }

// Luego en cada OutlinedTextField usa isError y supportingText.
// En el botón de registro, valida cada campo y asigna el error correspondiente.

    val isLoading by authViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            onValueChange = { name = it
                            nameError = null},
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError != null,
            supportingText = {
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it
                            emailError = null},
            label = { Text("Correo universitario") },
            placeholder = { Text("nombre@alumno.ucm.es") },
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

        // Campo Contraseña
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

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it
                            confirmError = null},
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = confirmError != null,
            supportingText = {
                if (confirmError != null) {
                    Text(confirmError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = securityQuestion,
            onValueChange = { securityQuestion = it
                            questionError = null},
            label = { Text("Pregunta de seguridad") },
            placeholder = { Text("Ej: ¿Nombre de tu primera mascota?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = questionError != null,
            supportingText = {
                if (questionError != null) {
                    Text(questionError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = securityAnswer,
            onValueChange = { securityAnswer = it
                            answerError = null},
            label = { Text("Respuesta de seguridad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = answerError != null,
            supportingText = {
                if (answerError != null) {
                    Text(answerError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )


        Spacer(modifier = Modifier.height(8.dp))

        // Botón de registro
        Button(
            onClick = {
                nameError = null
                emailError = null
                passwordError = null
                confirmError = null
                questionError = null
                answerError = null

                var hasError = false
                // Validación local antes de llamar al ViewModel
//                if (password != confirmPassword) {
//                    showError = true
//                    errorMessage = "Las contraseñas no coinciden"
//                    return@Button
//                }

                if (name.isBlank()) {
                    nameError = "El nombre es obligatorio"
                    hasError = true
                }
                if (!authViewModel.isValidUniversityEmail(email)) {
                    emailError = "Correo no válido. Usa uno universitario"
                    hasError = true
                } else if (email.isBlank()) {
                    emailError = "El correo es obligatorio"
                    hasError = true
                }
                if (password.isBlank()) {
                    passwordError = "La contraseña es obligatoria"
                    hasError = true
                } else if (password.length < 4) {
                    passwordError = "Mínimo 4 caracteres"
                    hasError = true
                }
                if (password.isBlank()) {
                    confirmError = "Repite la contraseña"
                    hasError = true
                }
                if (confirmPassword != password) {
                    confirmError = "Las contraseñas no coinciden"
                    hasError = true
                }
                if (securityQuestion.isBlank()) {
                    questionError = "La pregunta de seguridad es obligatoria"
                    hasError = true
                }
                if (securityAnswer.isBlank()) {
                    answerError = "La respuesta de seguridad es obligatoria"
                    hasError = true
                }

                if (!hasError) {
                    authViewModel.registerUser(
                        name, email, password, securityQuestion, securityAnswer
                    ) { success, message ->
                        if (success) onRegisterSuccess()
                        else {
                            // Si el error es global (ej. correo ya existe), mostrar en un diálogo o Toast
                            emailError = message
                        }
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
