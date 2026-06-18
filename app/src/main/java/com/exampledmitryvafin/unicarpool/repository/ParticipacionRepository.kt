package com.exampledmitryvafin.unicarpool.repository

import com.exampledmitryvafin.unicarpool.data.dao.ParticipacionDao
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import kotlinx.coroutines.flow.Flow

class ParticipacionRepository(private val participacionDao: ParticipacionDao) {


    fun getParticipacionesActivasByUsuarioFlow(usuarioId: Long): Flow<List<Participacion>> {
        return participacionDao.getParticipacionesActivasByUsuarioFlow(usuarioId)
    }

    fun getParticipacionesByUsuarioFlow(usuarioId: Long): Flow<List<Participacion>> {
        return participacionDao.getParticipacionesByUsuarioFlow(usuarioId)
    }

    fun getParticipacionesByViajeFlow(viajeId: Long): Flow<List<Participacion>> {
        return participacionDao.getParticipacionesByViajeFlow(viajeId)
    }


    suspend fun getParticipacionesActivasByUsuario(usuarioId: Long): List<Participacion> {
        return participacionDao.getParticipacionesActivasByUsuario(usuarioId)
    }

    suspend fun getParticipacionesByUsuario(usuarioId: Long): List<Participacion> {
        return participacionDao.getParticipacionesByUsuario(usuarioId)
    }

    suspend fun getParticipantesByViaje(viajeId: Long): List<Participacion> {
        return participacionDao.getParticipacionesByViaje(viajeId)
    }

    suspend fun isUserInRide(usuarioId: Long, viajeId: Long): Boolean {
        return participacionDao.getParticipacion(usuarioId, viajeId) != null
    }

    suspend fun addParticipacion(participacion: Participacion): Long {
        return participacionDao.insert(participacion)
    }

    suspend fun cancelarParticipacion(usuarioId: Long, viajeId: Long) {
        participacionDao.cancelarParticipacion(usuarioId, viajeId)
    }

    suspend fun removeParticipacion(usuarioId: Long, viajeId: Long) {
        participacionDao.deleteParticipacion(usuarioId, viajeId)
    }

    suspend fun deleteParticipacionById(participacionId: Long) {
        participacionDao.deleteParticipacionById(participacionId)
    }

    suspend fun deleteByViajeId(viajeId: Long) {
        participacionDao.deleteByViajeId(viajeId)
    }

    suspend fun updatePassengerName(userId: Long, newName: String) {
        participacionDao.updatePassengerNameInParticipaciones(userId, newName)
    }

    suspend fun hasOverlappingPassengerRide(
        pasajeroId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean {
        return participacionDao.hasOverlappingPassengerRide(pasajeroId, fechaSalida, horaSalida, horaLlegada)
    }

    fun getParticipacionesActivasByUsuarioAsFlow(usuarioId: Long): Flow<List<Participacion>> {
        return participacionDao.getParticipacionesActivasByUsuarioAsFlow(usuarioId)
    }

    suspend fun hasOverlappingPassengerRideForConductor(
        usuarioId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean {
        return participacionDao.hasOverlappingPassengerRideForConductor(usuarioId, fechaSalida, horaSalida, horaLlegada)
    }


    fun getParticipacionesActivasWithViaje(usuarioId: Long): Flow<List<Participacion>> {
        return participacionDao.getParticipacionesActivasWithViaje(usuarioId)
    }

    suspend fun deleteParticipacionByUserAndRide(usuarioId: Long, viajeId: Long) {
        participacionDao.deleteParticipacionByUserAndRide(usuarioId, viajeId)
    }
}