package com.exampledmitryvafin.unicarpool.repository

import com.exampledmitryvafin.unicarpool.data.dao.ViajeDao
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import kotlinx.coroutines.flow.Flow

class ViajeRepository(private val viajeDao: ViajeDao) {

    // Obtener todos los viajes activos (observable en tiempo real)
    fun getAllViajesActivos(): Flow<List<Viaje>> {
        return viajeDao.getAllViajesActivos()
    }

    // Buscar viajes por filtros (Flow)
    fun searchViajes(origen: String, destino: String, fecha: String): Flow<List<Viaje>> {
        return viajeDao.searchViajes(origen, destino, fecha)
    }

    // Obtener viajes activos de un conductor específico (suspend)
    suspend fun getViajesActivosByConductor(conductorId: Long): List<Viaje> {
        return viajeDao.getViajesActivosByConductor(conductorId)
    }

    // Obtener un viaje por su ID (suspend)
    suspend fun getViajeById(viajeId: Long): Viaje? {
        return viajeDao.getViajeById(viajeId)
    }

    // Crear un nuevo viaje (suspend)
    suspend fun createViaje(viaje: Viaje): Long {
        return viajeDao.insert(viaje)
    }

    // Actualizar un viaje (suspend)
    suspend fun updateViaje(viaje: Viaje) {
        viajeDao.update(viaje)
    }

    // Cancelar un viaje (suspend)
    suspend fun cancelarViaje(viajeId: Long, descripcion: String) {
        viajeDao.cancelarViaje(viajeId, descripcion)
    }

    // Reducir plazas disponibles (suspend)
    suspend fun decrementPlazas(viajeId: Long) {
        viajeDao.decrementPlazas(viajeId)
    }

    // Aumentar plazas disponibles (suspend)
    suspend fun incrementPlazas(viajeId: Long) {
        viajeDao.incrementPlazas(viajeId)
    }

    // Obtener viajes por conductor como Flow
    fun getViajesByConductorAsFlow(conductorId: Long): Flow<List<Viaje>> {
        return viajeDao.getViajesByConductorAsFlow(conductorId)
    }

    // Obtener viajes por conductor como List (suspend - carga única)
    suspend fun getViajesByConductor(conductorId: Long): List<Viaje> {
        return viajeDao.getViajesByConductor(conductorId)
    }

    // Obtener todos los viajes activos como Flow
    fun getAllViajesActivosFlow(): Flow<List<Viaje>> {
        return viajeDao.getAllViajesActivos()
    }

    // Eliminar un viaje (suspend)
    suspend fun deleteViaje(viajeId: Long) {
        viajeDao.deleteViaje(viajeId)
    }

    suspend fun updateConductorName(userId: Long, newName: String) {
        viajeDao.updateConductorNameInViajes(userId, newName)
    }

    fun searchViajesFiltrados(
        origen: String,
        destino: String,
        fecha: String,
        precioMax: Double,
        plazasMin: Int
    ): Flow<List<Viaje>> {
        return viajeDao.searchViajesFiltrados(origen, destino, fecha, precioMax, plazasMin)
    }

    suspend fun hasOverlappingRide(
        conductorId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean {
        return viajeDao.hasOverlappingRide(conductorId, fechaSalida, horaSalida, horaLlegada)
    }


    suspend fun updateCompletedViajes(fechaActual: String, horaActual: String) {
        viajeDao.updateCompletedViajes(fechaActual, horaActual)
    }

}