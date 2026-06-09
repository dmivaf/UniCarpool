package com.exampledmitryvafin.unicarpool.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exampledmitryvafin.unicarpool.data.database.AppDatabase
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import com.exampledmitryvafin.unicarpool.viewmodel.AuthViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.ProfileViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    val viajeRepository = ViajeRepository(database.viajeDao())
    val participacionRepository = ParticipacionRepository(database.participacionDao())

    val currentUserId by authViewModel.currentUserId.collectAsState()
    val currentUserName by authViewModel.currentUserName.collectAsState()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsState()

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            usuarioRepository,
            viajeRepository,
            participacionRepository,
            currentUserId
        )
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val viajesComoConductor by viewModel.viajesComoConductor.collectAsState()
    val viajesComoPasajero by viewModel.viajesComoPasajero.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(currentUserName) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var currentErrorMessage by remember { mutableStateOf("") }
    var currentSuccessMessage by remember { mutableStateOf("") }

    // Manejar errores
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && errorMessage!!.isNotBlank()) {
            currentErrorMessage = errorMessage!!
            showError = true
        }
    }

    // Manejar éxitos
    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage!!.isNotBlank()) {
            currentSuccessMessage = successMessage!!
            showSuccess = true
        }
    }

    // Diálogo de error
    if (showError) {
        AlertDialog(
            onDismissRequest = {
                showError = false
                viewModel.clearMessages()
            },
            title = { Text("Error", color = MaterialTheme.colorScheme.error) },
            text = { Text(currentErrorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showError = false
                    viewModel.clearMessages()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                viewModel.clearMessages()
            },
            title = { Text("Éxito", color = MaterialTheme.colorScheme.primary) },
            text = { Text(currentSuccessMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    viewModel.clearMessages()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo para editar nombre
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar nombre") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    // En el diálogo de edición, modifica el onClick:
                    onClick = {
                        viewModel.updateUserName(newName) { success, message ->
                            if (success) {
                                // Actualizar también en AuthViewModel
                                authViewModel.updateUserName(newName) { updated ->
                                    if (updated) {
                                        showEditDialog = false
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para eliminar cuenta
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text("¿Estás seguro de que quieres eliminar tu cuenta?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Esta acción es irreversible y eliminará:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("• Todos tus viajes como conductor", fontSize = 12.sp)
                    Text("• Tus participaciones en viajes", fontSize = 12.sp)
                    Text("• Tu información personal", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount { success, message ->
                            if (success) {
                                // Cerrar sesión después de eliminar la cuenta
                                authViewModel.logout { logoutSuccess ->
                                    if (logoutSuccess) {
                                        onLogout()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sí, eliminar mi cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar / Foto de perfil
        Surface(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del usuario
        Text(
            text = currentUserName.ifEmpty { "Usuario" },
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email del usuario
        Text(
            text = currentUserEmail,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        // Estadísticas
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Estadísticas",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🚗 Viajes como conductor:")
                    Text("$viajesComoConductor", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("👤 Viajes como pasajero:")
                    Text("$viajesComoPasajero", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📈 Total de viajes:")
                    Text(
                        "${viajesComoConductor + viajesComoPasajero}",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Editar perfil
        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("✏️ Editar nombre")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Cerrar sesión
        Button(
            onClick = {
                authViewModel.logout { success ->
                    if (success) {
                        onLogout()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = !isLoading
        ) {
            Text("🚪 Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Eliminar cuenta
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            enabled = !isLoading
        ) {
            Text("🗑️ Eliminar cuenta")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}