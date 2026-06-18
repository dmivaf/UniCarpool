package com.exampledmitryvafin.unicarpool.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exampledmitryvafin.unicarpool.data.database.AppDatabase
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import com.exampledmitryvafin.unicarpool.viewmodel.RideDetailViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.RideDetailViewModelFactory

@Composable
fun RideDetailScreen(
    rideId: Long,
    currentUserId: Long,
    currentUserName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val viajeRepository = ViajeRepository(database.viajeDao())
    val participacionRepository = ParticipacionRepository(database.participacionDao())
    val usuarioRepository = UsuarioRepository(database.usuarioDao())

    val viewModel: RideDetailViewModel = viewModel(
        factory = RideDetailViewModelFactory(
            viajeRepository,
            participacionRepository,
            usuarioRepository,
            currentUserId,
            currentUserName
        )
    )

    val viaje by viewModel.viaje.collectAsState()
    val pasajeros by viewModel.pasajeros.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUserJoined by viewModel.isUserJoined.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var currentErrorMessage by remember { mutableStateOf("") }
    var currentSuccessMessage by remember { mutableStateOf("") }

    LaunchedEffect(rideId) {
        viewModel.loadRideDetail(rideId)
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null && errorMessage!!.isNotBlank()) {
            currentErrorMessage = errorMessage!!
            showError = true
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage!!.isNotBlank()) {
            currentSuccessMessage = successMessage!!
            showSuccess = true
        }
    }

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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (viaje == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se encontró el viaje")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Volver")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${viaje!!.origen} → ${viaje!!.destino}",
                            fontSize = 24.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Divider()

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("📅 Fecha salida:", "${viaje!!.fecha_salida} ${viaje!!.hora_salida}")
                        InfoRow("📍 Fecha llegada:", "${viaje!!.fecha_llegada} ${viaje!!.hora_llegada}")
                        InfoRow("👤 Conductor:", "${viaje!!.nombre_conductor}")
                        InfoRow("💰 Precio:", "${viaje!!.precio}€ por persona")

                        Spacer(modifier = Modifier.height(8.dp))

                        val plazasColor = if (viaje!!.plazas_disponibles > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error

                        Text(
                            text = "Plazas: ${viaje!!.plazas_disponibles}/${viaje!!.plazas_totales} disponibles",
                            color = plazasColor,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }

            if (pasajeros.isNotEmpty()) {
                item {
                    Text(
                        text = "👥 Pasajeros (${pasajeros.size})",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }

                items(pasajeros) { pasajero ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("👤 Pasajero: #${pasajero.nombre_pasajero}")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                if (viaje?.estado == "activo") {
                    if (viewModel.isDriver()) {
                        var showCancelDialog by remember { mutableStateOf(false) }
                        var cancelDescription by remember { mutableStateOf("") }

                        Button(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar viaje")
                        }

                        if (showCancelDialog) {
                            AlertDialog(
                                onDismissRequest = { showCancelDialog = false },
                                title = { Text("Cancelar viaje", color = MaterialTheme.colorScheme.error) },
                                text = {
                                    Column {
                                        Text("¿Estás seguro de que quieres cancelar este viaje?")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = cancelDescription,
                                            onValueChange = { cancelDescription = it },
                                            label = { Text("Motivo (opcional)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.cancelRide(rideId, cancelDescription) { success, _ ->
                                                if (success) {
                                                    showCancelDialog = false
                                                    onBack()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Sí, cancelar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCancelDialog = false }) {
                                        Text("No, volver")
                                    }
                                }
                            )
                        }
                    } else {
                        if (isUserJoined) {
                            Button(
                                onClick = {
                                    viewModel.leaveRide(rideId) { success, message ->
                                        if (success) {
                                            viewModel.loadRideDetail(rideId)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Abandonar viaje")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (viewModel.canJoin()) {
                                        viewModel.joinRide(rideId) { success, message ->
                                            if (success) {
                                                viewModel.loadRideDetail(rideId)
                                            }
                                        }
                                    } else {
                                        val viajeActual = viaje
                                        if (viajeActual?.id_conductor == currentUserId) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Eres el conductor de este viaje",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (viajeActual?.plazas_disponibles == 0) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "No hay plazas disponibles",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = viewModel.canJoin()
                            ) {
                                Text("Unirse al viaje")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = if (viaje?.estado == "completado") "Este viaje ya ha finalizado." else "Este viaje fue cancelado.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        Text(text = value)
    }
}
