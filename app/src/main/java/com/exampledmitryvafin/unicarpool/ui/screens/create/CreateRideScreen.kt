package com.exampledmitryvafin.unicarpool.ui.screens.create

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    val context = LocalContext.current
    val database = com.exampledmitryvafin.unicarpool.data.database.AppDatabase.getInstance(context)
    val viajeRepository = com.exampledmitryvafin.unicarpool.repository.ViajeRepository(database.viajeDao())
    val participacionRepository = ParticipacionRepository(database.participacionDao())
    val focusManager = LocalFocusManager.current

    val viewModel: CreateRideViewModel = viewModel(
        factory = CreateRideViewModelFactory(viajeRepository, participacionRepository, currentUserId, currentUserName)
    )

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

    var errorOrigen by remember { mutableStateOf<String?>(null) }
    var errorDestino by remember { mutableStateOf<String?>(null) }
    var errorFechaSalida by remember { mutableStateOf<String?>(null) }
    var errorHoraSalida by remember { mutableStateOf<String?>(null) }
    var errorFechaLlegada by remember { mutableStateOf<String?>(null) }
    var errorHoraLlegada by remember { mutableStateOf<String?>(null) }
    var errorPlazas by remember { mutableStateOf<String?>(null) }
    var errorPrecio by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null && errorMessage!!.isNotBlank()) {
            currentErrorMessage = errorMessage!!
            showError = true
        }
    }

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

    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onRideCreated()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showError = true
        }
    }

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

        OutlinedTextField(
            value = origen,
            onValueChange = { viewModel.updateOrigen(it)
                            errorOrigen = null
                            },
            label = { Text("Origen") },
            isError = errorOrigen != null,
            supportingText = {
                if (errorOrigen != null) {
                    Text(errorOrigen!!, color = MaterialTheme.colorScheme.error)
                }
            },
            placeholder = { Text("Ej: Avenida de Francia 30, Valencia") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = destino,
            onValueChange = { viewModel.updateDestino(it)
                            errorDestino = null},
            label = { Text("Destino") },
            isError = errorDestino != null,
            supportingText = {
                if (errorDestino != null) {
                    Text(errorDestino!!, color = MaterialTheme.colorScheme.error)
                }
            },
            placeholder = { Text("Ej: Universidad Politecnica de Valencia") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fechaSalida,
                onValueChange = {
                    viewModel.updateFechaSalida(it)
                    errorFechaSalida = null
                },
                label = { Text("Fecha de salida") },
                isError = errorFechaSalida != null,
                supportingText = {
                    if (errorFechaSalida != null) {
                        Text(errorFechaSalida!!, color = MaterialTheme.colorScheme.error)
                    }
                },
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
                onValueChange = {
                    viewModel.updateHoraSalida(it)
                    errorHoraSalida = null
                },
                label = { Text("Hora de salida") },
                isError = errorHoraSalida != null,
                supportingText = {
                    if (errorHoraSalida != null) {
                        Text(errorHoraSalida!!, color = MaterialTheme.colorScheme.error)
                    }
                },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fechaLlegada,
                onValueChange = {
                    viewModel.updateFechaLlegada(it)
                    errorFechaLlegada = null
                },
                label = { Text("Fecha de llegada") },
                isError = errorFechaLlegada != null,
                supportingText = {
                    if (errorFechaLlegada != null) {
                        Text(errorFechaLlegada!!, color = MaterialTheme.colorScheme.error)
                    }
                },
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
                onValueChange = {
                    viewModel.updateHoraLlegada(it)
                    errorHoraLlegada = null
                },
                label = { Text("Hora de llegada") },
                isError = errorHoraLlegada != null,
                supportingText = {
                    if (errorHoraLlegada != null) {
                        Text(errorHoraLlegada!!, color = MaterialTheme.colorScheme.error)
                    }
                },
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

        OutlinedTextField(
            value = plazas,
            onValueChange = { viewModel.updatePlazas(it)
                            errorPlazas = null},
            label = { Text("Plazas disponibles") },
            isError = errorPlazas != null,
            supportingText = {
                if (errorPlazas != null) {
                    Text(errorPlazas!!, color = MaterialTheme.colorScheme.error)
                }
            },
            placeholder = { Text("Ej: 3") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { viewModel.updatePrecio(it)
                            errorPrecio = null},
            label = { Text("Precio por pasajero (€)") },
            isError = errorPrecio != null,
            supportingText = {
                if (errorPrecio != null) {
                    Text(errorPrecio!!, color = MaterialTheme.colorScheme.error)
                }
            },
            placeholder = { Text("Ej: 5.50") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )

        Button(
            onClick = {
                errorOrigen = null
                errorDestino = null
                errorFechaSalida = null
                errorHoraSalida = null
                errorFechaLlegada = null
                errorHoraLlegada = null
                errorPlazas = null
                errorPrecio = null

                var hasError = false

                if (origen.isBlank()) {
                    errorOrigen = "El origen es obligatorio"
                    hasError = true
                }
                if (destino.isBlank()) {
                    errorDestino = "El destino es obligatorio"
                    hasError = true
                }
                if (fechaSalida.isBlank()) {
                    errorFechaSalida = "La fecha de salida es obligatoria"
                    hasError = true
                }

                if (fechaSalida<fechaLlegada) {
                    errorFechaSalida = "La fecha de salida no puede ser anterior a la fecha de llegada"
                    hasError = true
                }
                if (horaSalida.isBlank()) {
                    errorHoraSalida = "La hora de salida es obligatoria"
                    hasError = true
                }

                if(fechaLlegada==fechaSalida && horaSalida>horaLlegada){
                    errorHoraSalida = "La hora de salida no puede ser anterior a la hora de llegada"
                    hasError = true
                }
                if (fechaLlegada.isBlank()) {
                    errorFechaLlegada = "La fecha de llegada es obligatoria"
                    hasError = true
                }

                if(fechaLlegada<fechaSalida){
                    errorFechaLlegada = "La fecha de llegada no puede ser anterior a la fecha de salida"
                    hasError = true
                }

                if (horaLlegada.isBlank()) {
                    errorHoraLlegada = "La hora de llegada es obligatoria"
                    hasError = true
                }
                val plazasInt = plazas.toIntOrNull()
                if (plazasInt == null || plazasInt < 1) {
                    errorPlazas = "Debe ser un número mayor o igual a 1"
                    hasError = true
                }
                val precioDouble = precio.toDoubleOrNull()
                if (precioDouble == null || precioDouble < 0) {
                    errorPrecio = "Debe ser un número mayor o igual a 0"
                    hasError = true
                }

                if (!hasError) {
                    focusManager.clearFocus()
                    viewModel.createRide { success, message ->
                        if (!success) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        ) {
            Text("Publicar Viaje")
        }
    }
}