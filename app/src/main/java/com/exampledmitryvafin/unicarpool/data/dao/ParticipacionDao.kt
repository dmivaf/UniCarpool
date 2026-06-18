package com.exampledmitryvafin.unicarpool.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipacionDao {

    // === Funciones que devuelven Flow (observables en tiempo real) ===

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId AND estado_participacion = 'activa'")
    fun getParticipacionesActivasByUsuarioFlow(usuarioId: Long): Flow<List<Participacion>>

    @Query("SELECT * FROM participaciones WHERE id_viaje = :viajeId")
    fun getParticipacionesByViajeFlow(viajeId: Long): Flow<List<Participacion>>

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId")
    fun getParticipacionesByUsuarioFlow(usuarioId: Long): Flow<List<Participacion>>

    // === Funciones suspend (carga única) ===

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId AND estado_participacion = 'activa'")
    suspend fun getParticipacionesActivasByUsuario(usuarioId: Long): List<Participacion>

    @Query("SELECT * FROM participaciones WHERE id_viaje = :viajeId")
    suspend fun getParticipacionesByViaje(viajeId: Long): List<Participacion>

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId")
    suspend fun getParticipacionesByUsuario(usuarioId: Long): List<Participacion>

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId AND id_viaje = :viajeId")
    suspend fun getParticipacion(usuarioId: Long, viajeId: Long): Participacion?

    @Insert
    suspend fun insert(participacion: Participacion): Long

    @Query("UPDATE participaciones SET estado_participacion = 'cancelada' WHERE id_usuario = :usuarioId AND id_viaje = :viajeId")
    suspend fun cancelarParticipacion(usuarioId: Long, viajeId: Long)

    @Query("DELETE FROM participaciones WHERE id_usuario = :usuarioId AND id_viaje = :viajeId")
    suspend fun deleteParticipacion(usuarioId: Long, viajeId: Long)

    @Query("DELETE FROM participaciones WHERE id_participacion = :participacionId")
    suspend fun deleteParticipacionById(participacionId: Long)

    @Query("DELETE FROM participaciones WHERE id_viaje = :viajeId")
    suspend fun deleteByViajeId(viajeId: Long)

    @Query("UPDATE participaciones SET nombre_pasajero = :newName WHERE id_usuario = :userId")
    suspend fun updatePassengerNameInParticipaciones(userId: Long, newName: String)

    // Comprobar si el pasajero tiene una participación activa en un viaje que solape con el nuevo
    @Query("""
    SELECT COUNT(*) > 0 FROM participaciones p
    JOIN viajes v ON p.id_viaje = v.id_viaje
    WHERE p.id_usuario = :pasajeroId 
    AND p.estado_participacion = 'activa'
    AND v.estado = 'activo'
    AND v.fecha_salida = :fechaSalida
    AND (
        (v.hora_salida <= :horaLlegada AND v.hora_llegada >= :horaSalida)
    )
""")
    suspend fun hasOverlappingPassengerRide(
        pasajeroId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean

    @Query("SELECT * FROM participaciones WHERE id_usuario = :usuarioId AND estado_participacion = 'activa'")
    fun getParticipacionesActivasByUsuarioAsFlow(usuarioId: Long): Flow<List<Participacion>>

    // En ParticipacionDao.kt

    @Query("""
    SELECT COUNT(*) > 0 FROM participaciones p
    JOIN viajes v ON p.id_viaje = v.id_viaje
    WHERE p.id_usuario = :usuarioId
    AND p.estado_participacion = 'activa'
    AND v.estado = 'activo'
    AND v.fecha_salida = :fechaSalida
    AND (
        (v.hora_salida <= :horaLlegada AND v.hora_llegada >= :horaSalida)
    )
""")
    suspend fun hasOverlappingPassengerRideForConductor(
        usuarioId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean

    // En ParticipacionDao.kt

    @Query("""
    SELECT p.* FROM participaciones p
    JOIN viajes v ON p.id_viaje = v.id_viaje
    WHERE p.id_usuario = :usuarioId 
    AND p.estado_participacion = 'activa'
    AND v.estado IN ('activo', 'completado', 'cancelado')
    ORDER BY v.fecha_salida, v.hora_salida
""")
    fun getParticipacionesActivasWithViaje(usuarioId: Long): Flow<List<Participacion>>

    @Query("DELETE FROM participaciones WHERE id_usuario = :usuarioId AND id_viaje = :viajeId")
    suspend fun deleteParticipacionByUserAndRide(usuarioId: Long, viajeId: Long)
}

