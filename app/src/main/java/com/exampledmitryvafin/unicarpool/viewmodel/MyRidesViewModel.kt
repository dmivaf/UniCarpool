package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyRidesViewModel(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _viajesComoConductor = MutableStateFlow<List<Viaje>>(emptyList())
    val viajesComoConductor: StateFlow<List<Viaje>> = _viajesComoConductor.asStateFlow()

    private val _viajesComoPasajero = MutableStateFlow<List<Pair<Viaje, Participacion>>>(emptyList())
    val viajesComoPasajero: StateFlow<List<Pair<Viaje, Participacion>>> = _viajesComoPasajero.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyRides()
    }

    private fun loadMyRides() {
        // Observar viajes como conductor (Flow continuo)
        viewModelScope.launch {
            viajeRepository.getViajesByConductorAsFlow(currentUserId).collect { viajes ->
                _viajesComoConductor.value = viajes.filter { it.estado == "activo" }
            }
        }

        // Observar participaciones activas y luego cargar los viajes correspondientes
        viewModelScope.launch {
            participacionRepository.getParticipacionesActivasByUsuarioAsFlow(currentUserId).collect { participaciones ->
                val pasajeroRides = mutableListOf<Pair<Viaje, Participacion>>()
                for (participacion in participaciones) {
                    val viaje = viajeRepository.getViajeById(participacion.id_viaje)
                    if (viaje != null && viaje.estado == "activo") {
                        pasajeroRides.add(Pair(viaje, participacion))
                    }
                }
                _viajesComoPasajero.value = pasajeroRides
            }
        }
    }

    fun getPlazasOcupadas(viaje: Viaje): Int {
        return viaje.plazas_totales - viaje.plazas_disponibles
    }
    // Al principio, junto a los otros StateFlow
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Opcional: método para limpiar el error
    fun clearError() {
        _errorMessage.value = null
    }
}