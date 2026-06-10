package com.exampledmitryvafin.unicarpool.ui.screens.myrides

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exampledmitryvafin.unicarpool.data.database.AppDatabase
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import com.exampledmitryvafin.unicarpool.viewmodel.MyRidesViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.MyRidesViewModelFactory
import com.exampledmitryvafin.unicarpool.viewmodel.RideRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRidesScreen(
    currentUserId: Long,
    onRideClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val viajeRepository = ViajeRepository(database.viajeDao())
    val participacionRepository = ParticipacionRepository(database.participacionDao())

    val viewModel: MyRidesViewModel = viewModel(
        factory = MyRidesViewModelFactory(viajeRepository, participacionRepository, currentUserId)
    )

    val activeRides by viewModel.activeRides.collectAsState()
    val historyRides by viewModel.historyRides.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📌 Activos", "📜 Historial")

    // Filtro para activos (Todos, Conductor, Pasajero)
    var filterRole by remember { mutableStateOf<RideRole?>(null) }

    // Mostrar errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Mis Viajes",
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> { // Activos
                // Filtro de tipo
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterRole == null,
                        onClick = { filterRole = null },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = filterRole == RideRole.CONDUCTOR,
                        onClick = { filterRole = RideRole.CONDUCTOR },
                        label = { Text("🚗 Conductor") }
                    )
                    FilterChip(
                        selected = filterRole == RideRole.PASAJERO,
                        onClick = { filterRole = RideRole.PASAJERO },
                        label = { Text("👤 Pasajero") }
                    )
                }

                val filtered = when (filterRole) {
                    null -> activeRides
                    RideRole.CONDUCTOR -> activeRides.filter { it.role == RideRole.CONDUCTOR }
                    RideRole.PASAJERO -> activeRides.filter { it.role == RideRole.PASAJERO }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay viajes activos o cancelados")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { rideWithRole ->
                            val isCanceled = rideWithRole.viaje.estado == "cancelado"
                            val canSwipe = isCanceled && rideWithRole.role == RideRole.PASAJERO

                            if (canSwipe) {
                                SwipeToDeleteCard(
                                    viaje = rideWithRole.viaje,
                                    role = rideWithRole.role,
                                    isActive = rideWithRole.viaje.estado == "activo",
                                    onRideClick = { onRideClick(rideWithRole.viaje.id_viaje) },
                                    onDelete = { viewModel.deletePassengerParticipation(rideWithRole.viaje.id_viaje) }
                                )
                            } else {
                                RideItemCard(
                                    viaje = rideWithRole.viaje,
                                    role = rideWithRole.role,
                                    isActive = rideWithRole.viaje.estado == "activo",
                                    onRideClick = { onRideClick(rideWithRole.viaje.id_viaje) }
                                )
                            }
                        }
                    }
                }
            }
            1 -> { // Historial (solo completados)
                if (historyRides.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay viajes completados")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyRides) { rideWithRole ->
                            val isCompleted = rideWithRole.viaje.estado == "completado"
                            val canSwipe = isCompleted && rideWithRole.role == RideRole.PASAJERO

                            if (canSwipe) {
                                SwipeToDeleteCard(
                                    viaje = rideWithRole.viaje,
                                    role = rideWithRole.role,
                                    isActive = false,
                                    onRideClick = { onRideClick(rideWithRole.viaje.id_viaje) },
                                    onDelete = { viewModel.deletePassengerParticipation(rideWithRole.viaje.id_viaje) }
                                )
                            } else {
                                RideItemCard(
                                    viaje = rideWithRole.viaje,
                                    role = rideWithRole.role,
                                    isActive = false,
                                    onRideClick = { onRideClick(rideWithRole.viaje.id_viaje) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RideItemCard(
    viaje: Viaje,
    role: RideRole,
    isActive: Boolean,
    onRideClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRideClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${viaje.origen} → ${viaje.destino}",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Badge(
                    containerColor = when (viaje.estado) {
                        "activo" -> if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                        "completado" -> Color.Green
                        "cancelado" -> Color.Red
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        when (viaje.estado) {
                            "activo" -> if (isActive) "Activo" else "Futuro"
                            "completado" -> "Completado"
                            "cancelado" -> "Cancelado"
                            else -> viaje.estado
                        },
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("📅 Salida: ${viaje.fecha_salida} ${viaje.hora_salida}", fontSize = 14.sp)
            Text("🕒 Llegada: ${viaje.fecha_llegada} ${viaje.hora_llegada}", fontSize = 14.sp)
            Text("👤 ${if (role == RideRole.CONDUCTOR) "Conductor: ${viaje.nombre_conductor}" else "Pasajero: tú"}", fontSize = 14.sp)

            if (viaje.estado == "cancelado" && viaje.descripcion_cancelacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Motivo: ${viaje.descripcion_cancelacion}",
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💰 ${viaje.precio}€ por persona",
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun SwipeToDeleteCard(
    viaje: Viaje,
    role: RideRole,
    isActive: Boolean,
    onRideClick: () -> Unit,
    onDelete: () -> Unit
) {
    val swipeEnabled = role == RideRole.PASAJERO && (viaje.estado == "cancelado" || viaje.estado == "completado")
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Fondo rojo para el swipe
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Red),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }

        // Tarjeta deslizable
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp)
                .clickable { if (offsetX == 0f) onRideClick() }
                .pointerInput(swipeEnabled) {
                    if (!swipeEnabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-200f, 0f)
                        },
                        onDragEnd = {
                            if (offsetX < -100f) {
                                onDelete()
                            }
                            offsetX = 0f
                        }
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            RideItemCard(
                viaje = viaje,
                role = role,
                isActive = isActive,
                onRideClick = { }
            )
        }
    }
}