package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideDetailViewModel(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val currentUserId: Long,
    private val currentUserName: String
) : ViewModel() {

    private val _viaje = MutableStateFlow<Viaje?>(null)
    val viaje: StateFlow<Viaje?> = _viaje.asStateFlow()

    private val _pasajeros = MutableStateFlow<List<Participacion>>(emptyList())
    val pasajeros: StateFlow<List<Participacion>> = _pasajeros.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUserJoined = MutableStateFlow(false)
    val isUserJoined: StateFlow<Boolean> = _isUserJoined.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadRideDetail(rideId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar datos del viaje
                val viajeData = viajeRepository.getViajeById(rideId)
                _viaje.value = viajeData

                // Cargar pasajeros
                if (viajeData != null) {
                    val pasajerosData = participacionRepository.getParticipantesByViaje(rideId)
                    _pasajeros.value = pasajerosData

                    // Verificar si el usuario actual ya está apuntado
                    val joined = participacionRepository.isUserInRide(currentUserId, rideId)
                    _isUserJoined.value = joined
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinRide(rideId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Obtener información del usuario actual
                val usuario = usuarioRepository.getUserById(currentUserId)
                if (usuario == null) {
                    onResult(false, "Usuario no encontrado")
                    return@launch
                }

                // Verificar que no esté ya apuntado
                if (_isUserJoined.value) {
                    onResult(false, "Ya estás apuntado a este viaje")
                    return@launch
                }

                // Verificar que haya plazas disponibles
                val viajeActual = _viaje.value
                if (viajeActual == null) {
                    onResult(false, "Viaje no encontrado")
                    return@launch
                }

                if (viajeActual.plazas_disponibles <= 0) {
                    onResult(false, "No hay plazas disponibles en este viaje")
                    return@launch
                }

                // Verificar solapamiento con otros viajes del pasajero
                val hasOverlap = participacionRepository.hasOverlappingPassengerRide(
                    pasajeroId = currentUserId,
                    fechaSalida = viajeActual.fecha_salida,
                    horaSalida = viajeActual.hora_salida,
                    horaLlegada = viajeActual.hora_llegada
                )

                if (hasOverlap) {
                    _errorMessage.value = "No puedes unirte a este viaje porque ya tienes otro viaje activo en el mismo horario"
                    onResult(false, "Ya estás apuntado a otro viaje con horario solapado")
                    return@launch
                }

                // En joinRide, después de verificar plazas y antes de crear participación:

                // Verificar si el usuario tiene algún viaje como conductor que solape con este viaje
                val hasOverlapAsDriver = viajeRepository.hasOverlappingRide(
                    conductorId = currentUserId,
                    fechaSalida = viajeActual.fecha_salida,
                    horaSalida = viajeActual.hora_salida,
                    horaLlegada = viajeActual.hora_llegada
                )

                if (hasOverlapAsDriver) {
                    _errorMessage.value = "Ya eres conductor de otro viaje con horario solapado. No puedes unirte como pasajero."
                    onResult(false, "Ya eres conductor de otro viaje con horario solapado")
                    return@launch
                }

                // Crear participación
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val participacion = Participacion(
                    id_usuario = currentUserId,
                    nombre_pasajero = usuario.nombre,
                    id_viaje = rideId,
                    fecha_union = currentDate,
                    estado_participacion = "activa"
                )

                val participacionId = participacionRepository.addParticipacion(participacion)

                if (participacionId > 0) {
                    viajeRepository.decrementPlazas(rideId)
                    _isUserJoined.value = true
                    _successMessage.value = "Te has unido al viaje correctamente"
                    onResult(true, "Te has unido al viaje")
                    loadRideDetail(rideId)
                } else {
                    onResult(false, "Error al unirte al viaje")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun leaveRide(rideId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Verificar que esté apuntado
                if (!_isUserJoined.value) {
                    onResult(false, "No estás apuntado a este viaje")
                    return@launch
                }

                // Cancelar participación
                participacionRepository.removeParticipacion(currentUserId, rideId)

                // Incrementar plazas disponibles
                viajeRepository.incrementPlazas(rideId)

                // Actualizar estado local
                _isUserJoined.value = false
                _successMessage.value = "Has abandonado el viaje"
                onResult(true, "Has abandonado el viaje")

                // Recargar datos
                loadRideDetail(rideId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelRide(rideId: Long, description: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (!isDriver()) {
                    onResult(false, "Solo el conductor puede cancelar")
                    return@launch
                }
                viajeRepository.cancelarViaje(rideId, description)
                participacionRepository.deleteByViajeId(rideId) // Eliminar todas las participaciones
                _successMessage.value = "Viaje cancelado"
                onResult(true, "Cancelado")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false, e.message ?: "Error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun canJoin(): Boolean {
        val viajeActual = _viaje.value
        return !_isUserJoined.value &&
                viajeActual != null &&
                viajeActual.plazas_disponibles > 0 &&
                viajeActual.id_conductor != currentUserId  // El conductor no puede unirse a su propio viaje
    }

    fun isDriver(): Boolean {
        return _viaje.value?.id_conductor == currentUserId
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}