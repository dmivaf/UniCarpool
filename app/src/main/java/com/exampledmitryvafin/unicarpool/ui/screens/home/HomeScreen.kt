package com.exampledmitryvafin.unicarpool.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.ui.components.DatePickerDialog
import com.exampledmitryvafin.unicarpool.viewmodel.ViajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viajeViewModel: ViajeViewModel,
    onRideClick: (Long) -> Unit
) {
    val viajes by viajeViewModel.viajesDisponibles.collectAsState()
    val isLoading by viajeViewModel.isLoading.collectAsState()

    // Estados de filtros
    val origen by viajeViewModel.origenBusqueda.collectAsState()
    val destino by viajeViewModel.destinoBusqueda.collectAsState()
    val fecha by viajeViewModel.fechaBusqueda.collectAsState()
    val precioMax by viajeViewModel.precioMax.collectAsState()
    val plazasMinText by viajeViewModel.plazasMinText.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "¡Bienvenido a UniCarpool!",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "Encuentra viajes compartidos a tu universidad",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Botón para mostrar/ocultar filtros
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showFilters = !showFilters },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, contentDescription = "Filtros")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filtros de búsqueda")
                }
                Icon(
                    if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showFilters) "Ocultar" else "Mostrar"
                )
            }
        }

        // Panel de filtros (colapsable)
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Origen
                    OutlinedTextField(
                        value = origen,
                        onValueChange = { viajeViewModel.updateOrigenBusqueda(it) },
                        label = { Text("Origen") },
                        placeholder = { Text("Ciudad, universidad...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Destino
                    OutlinedTextField(
                        value = destino,
                        onValueChange = { viajeViewModel.updateDestinoBusqueda(it) },
                        label = { Text("Destino") },
                        placeholder = { Text("Ciudad, universidad...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            label = { Text("Fecha (YYYY-MM-DD)") },
                            placeholder = { Text("Ej: 2025-03-20") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                        Button(onClick = { showDatePicker = true }) {
                            Text("📅")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Precio máximo
                    OutlinedTextField(
                        value = if (precioMax > 0) precioMax.toString() else "",
                        onValueChange = {
                            val value = it.toDoubleOrNull()
                            viajeViewModel.updatePrecioMax(value ?: 0.0)
                        },
                        label = { Text("Precio máximo (€)") },
                        placeholder = { Text("Sin límite") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    // Plazas disponibles mínimas
                    Text("Plazas disponibles:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Plazas disponibles mínimas
                        OutlinedTextField(
                            value = plazasMinText,
                            onValueChange = { viajeViewModel.updatePlazasMinText(it) },
                            label = { Text("Plazas mínimas") },
                            placeholder = { Text("Ej: 2 (mostrar viajes con al menos 2 plazas)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                // Al final del panel de filtros, antes de cerrar el Card
                Button(
                    onClick = {
                        viajeViewModel.updateOrigenBusqueda("")
                        viajeViewModel.updateDestinoBusqueda("")
                        viajeViewModel.updateFechaBusqueda("")
                        viajeViewModel.updatePrecioMax(0.0)
                        viajeViewModel.updatePlazasMinText("")   // NUEVO
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Limpiar filtros")
                }
            }
        }

        // Resultados
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (viajes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay viajes disponibles con los filtros actuales")
                    Text(
                        text = "Prueba otros filtros o crea un viaje",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viajes) { viaje ->
                    RideCard(
                        viaje = viaje,
                        onClick = { onRideClick(viaje.id_viaje) }
                    )
                }
            }
        }
    }

    // DatePicker dialog
    if (showDatePicker) {
        DatePickerDialog { date ->
            viajeViewModel.updateFechaBusqueda(date)
            showDatePicker = false
        }
    }
}

@Composable
fun RideCard(
    viaje: Viaje,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${viaje.origen} → ${viaje.destino}",
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 ${viaje.fecha_salida}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "👤 Conductor: ${viaje.nombre_conductor}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${viaje.plazas_disponibles}/${viaje.plazas_totales} plazas",
                    fontSize = 12.sp,
                    color = if (viaje.plazas_disponibles > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💰 ${viaje.precio}€",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}