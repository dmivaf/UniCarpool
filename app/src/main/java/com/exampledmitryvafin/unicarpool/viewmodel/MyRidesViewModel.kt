package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class RideRole { CONDUCTOR, PASAJERO }
data class RideWithRole(val viaje: Viaje, val role: RideRole, val participacion: Participacion? = null)

class MyRidesViewModel(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Flujo combinado para viajes activos (futuros + cancelados)
    val activeRides: StateFlow<List<RideWithRole>> = combine(
        viajeRepository.getViajesByConductorAsFlow(currentUserId),
        participacionRepository.getParticipacionesActivasWithViaje(currentUserId)
    ) { viajesComoConductor, participaciones ->
        // Viajes como conductor: activos (futuros) o cancelados
        val conductorRides = viajesComoConductor
            .filter { (it.estado == "activo" && isFuture(it)) || it.estado == "cancelado" }
            .map { RideWithRole(it, RideRole.CONDUCTOR) }

        // Viajes como pasajero: activos (futuros) o cancelados
        val pasajeroRides = mutableListOf<RideWithRole>()
        for (p in participaciones) {
            val viaje = viajeRepository.getViajeById(p.id_viaje)
            if (viaje != null && ((viaje.estado == "activo" && isFuture(viaje)) || viaje.estado == "cancelado")) {
                pasajeroRides.add(RideWithRole(viaje, RideRole.PASAJERO, p))
            }
        }
        (conductorRides + pasajeroRides).sortedBy { it.viaje.fecha_salida + it.viaje.hora_salida }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flujo combinado para historial (solo completados)
    val historyRides: StateFlow<List<RideWithRole>> = combine(
        viajeRepository.getViajesByConductorAsFlow(currentUserId),
        participacionRepository.getParticipacionesActivasWithViaje(currentUserId)
    ) { viajesComoConductor, participaciones ->
        val conductorHistory = viajesComoConductor
            .filter { it.estado == "completado" }
            .map { RideWithRole(it, RideRole.CONDUCTOR) }

        val pasajeroHistory = mutableListOf<RideWithRole>()
        for (p in participaciones) {
            val viaje = viajeRepository.getViajeById(p.id_viaje)
            if (viaje != null && viaje.estado == "completado") {
                pasajeroHistory.add(RideWithRole(viaje, RideRole.PASAJERO, p))
            }
        }
        (conductorHistory + pasajeroHistory).sortedByDescending { it.viaje.fecha_salida + it.viaje.hora_salida }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun isFuture(viaje: Viaje): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val rideEndDateTime = sdf.parse("${viaje.fecha_llegada} ${viaje.hora_llegada}") ?: return false
            rideEndDateTime.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun deletePassengerParticipation(viajeId: Long) {
        viewModelScope.launch {
            try {
                participacionRepository.deleteParticipacionByUserAndRide(currentUserId, viajeId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}