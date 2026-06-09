package com.exampledmitryvafin.unicarpool.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.ui.components.DatePickerDialog
import com.exampledmitryvafin.unicarpool.ui.components.TimePickerDialog
import com.exampledmitryvafin.unicarpool.viewmodel.CreateRideViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.CreateRideViewModelFactory

@Composable
fun CreateRideScreen(
    currentUserId: Long,
    currentUserName: String,
    onRideCreated: () -> Unit
) {
    // Crear ViewModel con dependencias
    val context = LocalContext.current
    val database = com.exampledmitryvafin.unicarpool.data.database.AppDatabase.getInstance(context)
    val viajeRepository = com.exampledmitryvafin.unicarpool.repository.ViajeRepository(database.viajeDao())
    val participacionRepository = ParticipacionRepository(database.participacionDao())  // NUEVO


    val viewModel: CreateRideViewModel = viewModel(
        factory = CreateRideViewModelFactory(viajeRepository, participacionRepository, currentUserId, currentUserName)
    )

    // Observar estados
    val origen by viewModel.origen.collectAsState()
    val destino by viewModel.destino.collectAsState()
    val fechaSalida by viewModel.fechaSalida.collectAsState()
    val horaSalida by viewModel.horaSalida.collectAsState()
    val fechaLlegada by viewModel.fechaLlegada.collectAsState()
    val horaLlegada by viewModel.horaLlegada.collectAsState()
    val plazas by viewModel.plazas.collectAsState()
    val precio by viewModel.precio.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val success by viewModel.success.collectAsState()

    var showDatePickerSalida by remember { mutableStateOf(false) }
    var showTimePickerSalida by remember { mutableStateOf(false) }
    var showDatePickerLlegada by remember { mutableStateOf(false) }
    var showTimePickerLlegada by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var currentErrorMessage by remember { mutableStateOf("") }

    // Observar cambios en errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && errorMessage!!.isNotBlank()) {
            currentErrorMessage = errorMessage!!
            showError = true
        }
    }

    // Diálogo de error (con más visibilidad)
    if (showError) {
        AlertDialog(
            onDismissRequest = {
                showError = false
                viewModel.clearError()
            },
            title = {
                Text(
                    "Error en el formulario",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(currentErrorMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showError = false
                        viewModel.clearError()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Navegar cuando el viaje se crea correctamente
    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onRideCreated()
        }
    }

    // Mostrar error si existe
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showError = true
        }
    }

    // Mostrar DatePicker cuando se solicita
    if (showDatePickerSalida) {
        DatePickerDialog { date ->
            viewModel.updateFechaSalida(date)
            showDatePickerSalida = false
        }
    }

    if (showDatePickerLlegada) {
        DatePickerDialog { date ->
            viewModel.updateFechaLlegada(date)
            showDatePickerLlegada = false
        }
    }

    if (showTimePickerSalida) {
        TimePickerDialog { time ->
            viewModel.updateHoraSalida(time)
            showTimePickerSalida = false
        }
    }

    if (showTimePickerLlegada) {
        TimePickerDialog { time ->
            viewModel.updateHoraLlegada(time)
            showTimePickerLlegada = false
        }
    }

    // Diálogo de error
    if (showError && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showError = false
                viewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = {
                    showError = false
                    viewModel.clearError()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Formulario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Publicar nuevo viaje",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Completa los datos para compartir tu viaje",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Origen
        OutlinedTextField(
            value = origen,
            onValueChange = { viewModel.updateOrigen(it) },
            label = { Text("Origen") },
            placeholder = { Text("Ej: Plaza Mayor, Madrid") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Destino
        OutlinedTextField(
            value = destino,
            onValueChange = { viewModel.updateDestino(it) },
            label = { Text("Destino") },
            placeholder = { Text("Ej: Universidad Complutense") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Fecha y Hora de Salida
        Text(
            text = "Fecha y hora de salida",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fechaSalida,
                onValueChange = {},
                label = { Text("Fecha") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePickerSalida = true }) {
                        Text("📅")
                    }
                }
            )

            OutlinedTextField(
                value = horaSalida,
                onValueChange = {},
                label = { Text("Hora") },
                placeholder = { Text("HH:MM") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePickerSalida = true }) {
                        Text("⏰")
                    }
                }
            )
        }

        // Fecha y Hora de Llegada
        Text(
            text = "Fecha y hora de llegada prevista",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fechaLlegada,
                onValueChange = {},
                label = { Text("Fecha") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePickerLlegada = true }) {
                        Text("📅")
                    }
                }
            )

            OutlinedTextField(
                value = horaLlegada,
                onValueChange = {},
                label = { Text("Hora") },
                placeholder = { Text("HH:MM") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePickerLlegada = true }) {
                        Text("⏰")
                    }
                }
            )
        }

        // Plazas
        OutlinedTextField(
            value = plazas,
            onValueChange = { viewModel.updatePlazas(it) },
            label = { Text("Plazas disponibles") },
            placeholder = { Text("Ej: 3") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )

        // Precio
        OutlinedTextField(
            value = precio,
            onValueChange = { viewModel.updatePrecio(it) },
            label = { Text("Precio por pasajero (€)") },
            placeholder = { Text("Ej: 5.50") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Botón de publicar
        Button(
            onClick = { viewModel.createRide { _, _ -> } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Publicar viaje")
            }
        }
    }
}