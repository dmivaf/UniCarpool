package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateRideViewModel(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long,
    private val currentUserName: String
) : ViewModel() {

    // Estados del formulario
    private val _origen = MutableStateFlow("")
    val origen: StateFlow<String> = _origen.asStateFlow()

    private val _destino = MutableStateFlow("")
    val destino: StateFlow<String> = _destino.asStateFlow()

    private val _fechaSalida = MutableStateFlow("")
    val fechaSalida: StateFlow<String> = _fechaSalida.asStateFlow()

    private val _horaSalida = MutableStateFlow("")
    val horaSalida: StateFlow<String> = _horaSalida.asStateFlow()

    private val _fechaLlegada = MutableStateFlow("")
    val fechaLlegada: StateFlow<String> = _fechaLlegada.asStateFlow()

    private val _horaLlegada = MutableStateFlow("")
    val horaLlegada: StateFlow<String> = _horaLlegada.asStateFlow()

    private val _plazas = MutableStateFlow("")
    val plazas: StateFlow<String> = _plazas.asStateFlow()

    private val _precio = MutableStateFlow("")
    val precio: StateFlow<String> = _precio.asStateFlow()

    // Estado de carga y errores
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    // Funciones para actualizar campos
    fun updateOrigen(value: String) { _origen.value = value }
    fun updateDestino(value: String) { _destino.value = value }
    fun updateFechaSalida(value: String) { _fechaSalida.value = value }
    fun updateHoraSalida(value: String) { _horaSalida.value = value }
    fun updateFechaLlegada(value: String) { _fechaLlegada.value = value }
    fun updateHoraLlegada(value: String) { _horaLlegada.value = value }
    fun updatePlazas(value: String) { _plazas.value = value }
    fun updatePrecio(value: String) { _precio.value = value }
    fun clearError() { _errorMessage.value = null }

    // Validar que la fecha no sea anterior a hoy
    private fun isValidFecha(fecha: String): Boolean {
        if (fecha.isEmpty()) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        return fecha >= today
    }

    // Validar que la fecha de llegada no sea anterior a la fecha de salida
    // y que si es el mismo día, la hora de llegada sea posterior
    private fun isValidFechaLlegada(
        fechaSalida: String,
        horaSalida: String,
        fechaLlegada: String,
        horaLlegada: String
    ): Boolean {
        // Si la fecha de llegada es anterior a la de salida → inválido
        if (fechaLlegada < fechaSalida) return false

        // Si es el mismo día, comparar horas
        if (fechaLlegada == fechaSalida) {
            val horaSalidaInt = horaSalida.replace(":", "").toIntOrNull() ?: 0
            val horaLlegadaInt = horaLlegada.replace(":", "").toIntOrNull() ?: 0
            return horaLlegadaInt > horaSalidaInt
        }

        // Fecha de llegada posterior a fecha de salida → válido
        return true
    }

    // Validar que hora llegada sea posterior a hora salida (mismo día)
    private fun isValidHoraLlegada(fechaSalida: String, horaSalida: String, fechaLlegada: String, horaLlegada: String): Boolean {
        if (fechaSalida != fechaLlegada) return true  // Días diferentes, no podemos validar fácilmente

        val horaSalidaInt = horaSalida.replace(":", "").toIntOrNull() ?: 0
        val horaLlegadaInt = horaLlegada.replace(":", "").toIntOrNull() ?: 0
        return horaLlegadaInt > horaSalidaInt
    }

    // Validar que el conductor no tenga viaje activo en la misma fecha/hora
    private suspend fun hasConflictingRide(): Boolean {
        return viajeRepository.hasOverlappingRide(
            conductorId = currentUserId,
            fechaSalida = _fechaSalida.value,
            horaSalida = _horaSalida.value,
            horaLlegada = _horaLlegada.value
        )
    }

    // Crear el viaje
    fun createRide(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val origenValue = _origen.value
                val destinoValue = _destino.value
                val fechaSalidaValue = _fechaSalida.value
                val horaSalidaValue = _horaSalida.value
                val fechaLlegadaValue = _fechaLlegada.value
                val horaLlegadaValue = _horaLlegada.value
                val plazasValue = _plazas.value.toIntOrNull()
                val precioValue = _precio.value.toDoubleOrNull()

                // 1. Validar origen
                if (origenValue.isBlank()) {
                    _errorMessage.value = "El origen es obligatorio"
                    onResult(false, "El origen es obligatorio")
                    return@launch
                }

                // 2. Validar destino
                if (destinoValue.isBlank()) {
                    _errorMessage.value = "El destino es obligatorio"
                    onResult(false, "El destino es obligatorio")
                    return@launch
                }

                // 3. Validar fecha salida
                if (fechaSalidaValue.isBlank()) {
                    _errorMessage.value = "La fecha de salida es obligatoria"
                    onResult(false, "La fecha de salida es obligatoria")
                    return@launch
                }

                // 4. Validar que fecha salida no sea anterior a hoy
                if (!isValidFecha(fechaSalidaValue)) {
                    _errorMessage.value = "La fecha de salida no puede ser anterior a hoy"
                    onResult(false, "La fecha de salida no puede ser anterior a hoy")
                    return@launch
                }

                // 5. Validar hora salida
                if (horaSalidaValue.isBlank()) {
                    _errorMessage.value = "La hora de salida es obligatoria"
                    onResult(false, "La hora de salida es obligatoria")
                    return@launch
                }

                // 6. Validar fecha llegada
                if (fechaLlegadaValue.isBlank()) {
                    _errorMessage.value = "La fecha de llegada es obligatoria"
                    onResult(false, "La fecha de llegada es obligatoria")
                    return@launch
                }

                // 7. Validar hora llegada
                if (horaLlegadaValue.isBlank()) {
                    _errorMessage.value = "La hora de llegada es obligatoria"
                    onResult(false, "La hora de llegada es obligatoria")
                    return@launch
                }

                // 8. Validar que fecha llegada no sea anterior a fecha salida
                if (!isValidFechaLlegada(fechaSalidaValue, horaSalidaValue, fechaLlegadaValue, horaLlegadaValue)) {
                    _errorMessage.value = "La fecha y hora de llegada deben ser posteriores a la salida"
                    onResult(false, "La fecha y hora de llegada deben ser posteriores a la salida")
                    return@launch
                }

                // 9. Validar plazas
                if (plazasValue == null || plazasValue < 1) {
                    _errorMessage.value = "El número de plazas debe ser al menos 1"
                    onResult(false, "El número de plazas debe ser al menos 1")
                    return@launch
                }

                if (plazasValue > 8) {
                    _errorMessage.value = "El número máximo de plazas es 8"
                    onResult(false, "El número máximo de plazas es 8")
                    return@launch
                }

                // 10. Validar precio
                if (precioValue == null || precioValue < 0) {
                    _errorMessage.value = "El precio debe ser un número válido mayor o igual a 0"
                    onResult(false, "El precio debe ser un número válido mayor o igual a 0")
                    return@launch
                }

                if (precioValue > 100) {
                    _errorMessage.value = "El precio no puede superar los 100€"
                    onResult(false, "El precio no puede superar los 100€")
                    return@launch
                }

                // 11. Verificar conflictos con otros viajes del conductor
                // Verificar conflictos con otros viajes del conductor
                if (hasConflictingRide()) {
                    _errorMessage.value = "Ya tienes un viaje activo que coincide con este horario"
                    onResult(false, "Ya tienes un viaje activo que coincide con este horario")
                    return@launch
                }

                // Verificar si el usuario tiene algún viaje como pasajero que solape con este nuevo viaje
                val hasOverlapAsPassenger = participacionRepository.hasOverlappingPassengerRideForConductor(
                    usuarioId = currentUserId,
                    fechaSalida = _fechaSalida.value,
                    horaSalida = _horaSalida.value,
                    horaLlegada = _horaLlegada.value
                )

                if (hasOverlapAsPassenger) {
                    _errorMessage.value = "Ya eres pasajero en otro viaje con horario solapado. No puedes ser conductor al mismo tiempo."
                    onResult(false, "Ya eres pasajero en otro viaje con horario solapado")
                    return@launch
                }

                // Todas las validaciones pasaron -> crear viaje
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val nuevoViaje = Viaje(
                    id_conductor = currentUserId,
                    nombre_conductor = currentUserName,
                    origen = origenValue,
                    destino = destinoValue,
                    fecha_salida = fechaSalidaValue,
                    hora_salida = horaSalidaValue,
                    fecha_llegada = fechaLlegadaValue,
                    hora_llegada = horaLlegadaValue,
                    plazas_totales = plazasValue,
                    plazas_disponibles = plazasValue,
                    estado = "activo",
                    precio = precioValue,
                    created_at = currentDate
                )

                val rideId = viajeRepository.createViaje(nuevoViaje)

                if (rideId > 0) {
                    _success.value = true
                    onResult(true, "Viaje creado correctamente")
                } else {
                    _errorMessage.value = "Error al crear el viaje"
                    onResult(false, "Error al crear el viaje")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Resetear el estado de éxito (para navegar después)
    fun resetSuccess() {
        _success.value = false
    }
}