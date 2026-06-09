package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _viajesComoConductor = MutableStateFlow(0)
    val viajesComoConductor: StateFlow<Int> = _viajesComoConductor.asStateFlow()

    private val _viajesComoPasajero = MutableStateFlow(0)
    val viajesComoPasajero: StateFlow<Int> = _viajesComoPasajero.asStateFlow()

    init {
        loadUserStats()
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                val conductorRides = viajeRepository.getViajesByConductor(currentUserId)
                _viajesComoConductor.value = conductorRides.size

                val pasajeroRides = participacionRepository.getParticipacionesByUsuario(currentUserId)
                _viajesComoPasajero.value = pasajeroRides.size
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar estadísticas: ${e.message}"
            }
        }
    }

    fun updateUserName(newName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (newName.isBlank()) {
                    onResult(false, "El nombre no puede estar vacío")
                    return@launch
                }
                // Actualizar en tabla usuarios
                usuarioRepository.updateUserName(currentUserId, newName)

                // Actualizar en viajes donde es conductor
                viajeRepository.updateConductorName(currentUserId, newName)
                // Actualizar en participaciones donde es pasajero
                participacionRepository.updatePassengerName(currentUserId, newName)

                _successMessage.value = "Nombre actualizado correctamente"
                onResult(true, "Nombre actualizado")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun deleteAccount(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val participaciones = participacionRepository.getParticipacionesByUsuario(currentUserId)
                for (participacion in participaciones) {
                    participacionRepository.deleteParticipacionById(participacion.id_participacion)
                }

                val viajesConductor = viajeRepository.getViajesByConductor(currentUserId)
                for (viaje in viajesConductor) {
                    participacionRepository.deleteByViajeId(viaje.id_viaje)
                    viajeRepository.deleteViaje(viaje.id_viaje)
                }

                usuarioRepository.deleteUser(currentUserId)

                _successMessage.value = "Cuenta eliminada correctamente"
                onResult(true, "Cuenta eliminada")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}