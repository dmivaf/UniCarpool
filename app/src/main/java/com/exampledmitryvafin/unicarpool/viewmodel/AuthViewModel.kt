package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.datasource.DataStoreManager
import com.exampledmitryvafin.unicarpool.data.entity.Usuario
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // Estados de la UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserId = MutableStateFlow(-1L)
    val currentUserId: StateFlow<Long> = _currentUserId

    private val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail

    init {
        viewModelScope.launch {
            dataStoreManager.userId.collect {
                _currentUserId.value = it
            }
        }

        viewModelScope.launch {
            dataStoreManager.userName.collect {
                _currentUserName.value = it
            }
        }

        viewModelScope.launch {
            dataStoreManager.userEmail.collect {
                _currentUserEmail.value = it
            }
        }

        viewModelScope.launch {
            dataStoreManager.isLoggedIn.collect {
                _isLoggedIn.value = it
            }
        }
    }
    // Lista de dominios de correo universitario aceptados
    private val allowedDomains = listOf(
        "@alumno.ucm.es",
        "@ucm.es",
        "@upm.es",
        "@alumno.upm.es",
        "@uam.es",
        "@alumno.uam.es",
        "@uc3m.es",
        "@alumno.uc3m.es",
        "@urjc.es",
        "@uah.es",
        "@upv.es",
        "@alumno.upv.es",
        "@upv.edu.es",
        "@uv.es",
        "@alumni.uv.es"
    )

    // Validar que el correo sea universitario
    fun isValidUniversityEmail(email: String): Boolean {
        return allowedDomains.any { email.lowercase().endsWith(it) }
    }

    // Función para obtener la lista formateada (para el mensaje de error)
    private fun getAllowedDomainsList(): String {
        return allowedDomains.joinToString(", ")
    }

    // Validar que la contraseña no esté vacía
    fun isValidPassword(password: String): Boolean {
        return password.length >= 4
    }

    // Registrar nuevo usuario
    // En AuthViewModel.kt

    fun registerUser(
        name: String,
        email: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Validaciones
                if (name.isBlank()) {
                    onResult(false, "El nombre no puede estar vacío")
                    return@launch
                }
                if (!isValidUniversityEmail(email)) {
                    val domainsList = getAllowedDomainsList()
                    onResult(false, "Debes usar un correo universitario. Dominios aceptados: $domainsList")
                    return@launch
                }
                if (password.isBlank() || password.length < 4) {
                    onResult(false, "La contraseña debe tener al menos 4 caracteres")
                    return@launch
                }
                if (securityQuestion.isBlank()) {
                    onResult(false, "La pregunta de seguridad es obligatoria")
                    return@launch
                }
                if (securityAnswer.isBlank()) {
                    onResult(false, "La respuesta de seguridad es obligatoria")
                    return@launch
                }

                // Verificar si el usuario ya existe
                val existingUser = usuarioRepository.getUserByEmail(email)
                if (existingUser != null) {
                    onResult(false, "Ya existe una cuenta con este correo")
                    return@launch
                }

                // Crear nuevo usuario
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val newUser = Usuario(
                    nombre = name,
                    correo = email,
                    contrasena = password,
                    fecha_registro = currentDate,
                    preguntaSeguridad = securityQuestion,
                    respuestaSeguridad = securityAnswer
                )

                val userId = usuarioRepository.registerUser(newUser)

                if (userId > 0) {
                    dataStoreManager.saveUserSession(userId, name, email)
                    _isLoggedIn.value = true
                    _currentUserId.value = userId
                    _currentUserName.value = name
                    _currentUserEmail.value = email
                    onResult(true, "Registro exitoso")
                } else {
                    onResult(false, "Error al registrar usuario")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Iniciar sesión
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (email.isBlank()) {
                    onResult(false, "Introduce un correo electrónico")
                    return@launch
                }
                if (password.isBlank()) {
                    onResult(false, "Introduce la contraseña")
                    return@launch
                }

                // Primero comprobar si el correo existe
                val userExists = usuarioRepository.getUserByEmail(email) != null
                if (!userExists) {
                    onResult(false, "No existe ninguna cuenta con este correo electrónico")
                    return@launch
                }

                // Si existe, intentar login con contraseña
                val user = usuarioRepository.login(email, password)
                if (user != null) {
                    dataStoreManager.saveUserSession(user.id_usuario, user.nombre, user.correo)
                    _isLoggedIn.value = true
                    _currentUserId.value = user.id_usuario
                    _currentUserName.value = user.nombre
                    _currentUserEmail.value = user.correo
                    onResult(true, "Inicio de sesión exitoso")
                } else {
                    onResult(false, "Contraseña incorrecta")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Cerrar sesión
    fun logout(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                dataStoreManager.clearUserSession()
                _isLoggedIn.value = false
                _currentUserId.value = -1L
                _currentUserName.value = ""
                _currentUserEmail.value = ""
                onResult(true)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false)
            }
        }
    }

    fun updateUserName(newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val currentEmail = _currentUserEmail.value
                dataStoreManager.saveUserSession(_currentUserId.value, newName, currentEmail)
                _currentUserName.value = newName
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    suspend fun checkCurrentPassword(userId: Long, currentPassword: String): Boolean {
        val user = usuarioRepository.getUserById(userId) // necesitas este método
        return user?.contrasena == currentPassword
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validaciones
                if (currentPassword.isBlank()) {
                    onResult(false, "Debes introducir la contraseña actual")
                    return@launch
                }
                if (newPassword.isBlank()) {
                    onResult(false, "La nueva contraseña no puede estar vacía")
                    return@launch
                }
                if (newPassword.length < 4) {
                    onResult(false, "La nueva contraseña debe tener al menos 4 caracteres")
                    return@launch
                }
                if (newPassword != confirmPassword) {
                    onResult(false, "Las contraseñas nuevas no coinciden")
                    return@launch
                }

                val userId = _currentUserId.value
                if (userId == -1L) {
                    onResult(false, "Usuario no identificado")
                    return@launch
                }

                // Obtener usuario actual y verificar contraseña
                val user = usuarioRepository.getUserById(userId)
                if (user == null) {
                    onResult(false, "Usuario no encontrado")
                    return@launch
                }
                if (user.contrasena != currentPassword) {
                    onResult(false, "La contraseña actual es incorrecta")
                    return@launch
                }

                // Actualizar contraseña
                usuarioRepository.updatePassword(userId, newPassword)
                onResult(true, "Contraseña actualizada correctamente")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // En AuthViewModel.kt

    suspend fun recoverPassword(email: String, answer: String): String? {
        return try {
            val user = usuarioRepository.getUserByEmail(email)
            if (user != null && user.respuestaSeguridad.equals(answer, ignoreCase = true)) {
                user.contrasena
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // En AuthViewModel.kt

    suspend fun getSecurityQuestion(email: String): String? {
        return try {
            val user = usuarioRepository.getUserByEmail(email)
            user?.preguntaSeguridad
        } catch (e: Exception) {
            null
        }
    }
}