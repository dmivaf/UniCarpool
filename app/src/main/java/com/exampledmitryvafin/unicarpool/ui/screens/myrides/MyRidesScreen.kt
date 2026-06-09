    package com.exampledmitryvafin.unicarpool.ui.screens.myrides

    import android.widget.Toast
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
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.exampledmitryvafin.unicarpool.data.database.AppDatabase
    import com.exampledmitryvafin.unicarpool.data.entity.Viaje
    import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
    import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
    import com.exampledmitryvafin.unicarpool.viewmodel.MyRidesViewModel
    import com.exampledmitryvafin.unicarpool.viewmodel.MyRidesViewModelFactory

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

        val viajesComoConductor by viewModel.viajesComoConductor.collectAsState()
        val viajesComoPasajero by viewModel.viajesComoPasajero.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()   // ✅ ahora errorMessage es String?
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("🚗 Como conductor", "👤 Como pasajero")

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera
            Text(
                text = "Mis Viajes",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Pestañas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Contenido según pestaña seleccionada
            when (selectedTab) {
                0 -> {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (viajesComoConductor.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No has creado ningún viaje aún")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viajesComoConductor) { viaje ->
                                RideItemCard(
                                    viaje = viaje,
                                    plazasOcupadas = viewModel.getPlazasOcupadas(viaje),
                                    onClick = { onRideClick(viaje.id_viaje) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (viajesComoPasajero.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No te has unido a ningún viaje aún")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viajesComoPasajero) { (viaje, participacion) ->
                                RideItemCard(
                                    viaje = viaje,
                                    plazasOcupadas = viewModel.getPlazasOcupadas(viaje),
                                    onClick = { onRideClick(viaje.id_viaje) },
                                    isPassenger = true,
                                    joinedDate = participacion.fecha_union
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mostrar error si existe
        if (errorMessage != null) {
            LaunchedEffect(errorMessage) {
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    // Limpiar el error después de mostrarlo (opcional)
                    viewModel.clearError()
                }
            }
        }
    }

    @Composable
    fun RideItemCard(
        viaje: Viaje,
        plazasOcupadas: Int,
        onClick: () -> Unit,
        isPassenger: Boolean = false,
        joinedDate: String = ""
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Ruta
                Text(
                    text = "${viaje.origen} → ${viaje.destino}",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fecha y hora
                Text(
                    text = "📅 ${viaje.fecha_salida} ${viaje.hora_salida}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Plazas
                val plazasColor = if (viaje.plazas_disponibles > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error

                Text(
                    text = "👥 $plazasOcupadas/${viaje.plazas_totales} plazas ocupadas",
                    fontSize = 14.sp,
                    color = plazasColor
                )

                // Si es pasajero, mostrar cuándo se unió
                if (isPassenger) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📌 Te uniste: $joinedDate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Precio
                Text(
                    text = "💰 ${viaje.precio}€ por persona",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }