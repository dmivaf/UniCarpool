package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class ViajeViewModel(
    private val viajeRepository: ViajeRepository
) : ViewModel() {

    private val _viajesDisponibles = MutableStateFlow<List<Viaje>>(emptyList())
    val viajesDisponibles: StateFlow<List<Viaje>> = _viajesDisponibles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Añade estos estados
    private val _origenBusqueda = MutableStateFlow("")
    val origenBusqueda: StateFlow<String> = _origenBusqueda.asStateFlow()

    private val _destinoBusqueda = MutableStateFlow("")
    val destinoBusqueda: StateFlow<String> = _destinoBusqueda.asStateFlow()

    private val _fechaBusqueda = MutableStateFlow("")
    val fechaBusqueda: StateFlow<String> = _fechaBusqueda.asStateFlow()

    private val _precioMax = MutableStateFlow(0.0)
    val precioMax: StateFlow<Double> = _precioMax.asStateFlow()

    private var searchJob: Job? = null

    private val _plazasMinText = MutableStateFlow("")
    val plazasMinText: StateFlow<String> = _plazasMinText.asStateFlow()

    // Función para actualizar el texto
    fun updatePlazasMinText(value: String) {
        _plazasMinText.value = value
        triggerSearch()
    }

    fun updateOrigenBusqueda(value: String) {
        _origenBusqueda.value = value
        triggerSearch()
    }

    fun updateDestinoBusqueda(value: String) {
        _destinoBusqueda.value = value
        triggerSearch()
    }

    fun updateFechaBusqueda(value: String) {
        _fechaBusqueda.value = value
        triggerSearch()
    }

    fun updatePrecioMax(value: Double) {
        _precioMax.value = value
        triggerSearch()
    }

    // Obtener el valor numérico (0 si está vacío o no es número)
    private fun getPlazasMinValue(): Int {
        return _plazasMinText.value.toIntOrNull()?.takeIf { it >= 0 } ?: 0
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            viajeRepository.searchViajesFiltrados(
                origen = _origenBusqueda.value,
                destino = _destinoBusqueda.value,
                fecha = _fechaBusqueda.value,
                precioMax = _precioMax.value,
                plazasMin = getPlazasMinValue()   // Convertir a Int
            ).collect { viajes ->
                _viajesDisponibles.value = viajes
            }
        }
    }

    init {
        cargarViajesDisponibles()
    }

    fun cargarViajesDisponibles() {
        viewModelScope.launch {
            _isLoading.value = true
            viajeRepository.getAllViajesActivos().collect { viajes ->
                _viajesDisponibles.value = viajes
                _isLoading.value = false
            }
        }
    }
}