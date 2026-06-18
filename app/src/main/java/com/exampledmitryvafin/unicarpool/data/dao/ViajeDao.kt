package com.exampledmitryvafin.unicarpool.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.exampledmitryvafin.unicarpool.data.entity.Viaje
import kotlinx.coroutines.flow.Flow

@Dao
interface ViajeDao {
    @Query("SELECT * FROM viajes WHERE estado = 'activo' AND plazas_disponibles > 0 ORDER BY fecha_salida")
    fun getAllViajesActivos(): Flow<List<Viaje>>

    @Query("SELECT * FROM viajes WHERE id_conductor = :conductorId AND estado = 'activo'")
    suspend fun getViajesActivosByConductor(conductorId: Long): List<Viaje>

    @Query("SELECT * FROM viajes WHERE origen LIKE '%' || :origen || '%' AND destino LIKE '%' || :destino || '%' AND fecha_salida LIKE :fecha || '%' AND estado = 'activo'")
    fun searchViajes(origen: String, destino: String, fecha: String): Flow<List<Viaje>>

    @Insert
    suspend fun insert(viaje: Viaje): Long

    @Update
    suspend fun update(viaje: Viaje)

    @Query("UPDATE viajes SET plazas_disponibles = plazas_disponibles - 1 WHERE id_viaje = :viajeId AND plazas_disponibles > 0")
    suspend fun decrementPlazas(viajeId: Long)

    @Query("UPDATE viajes SET plazas_disponibles = plazas_disponibles + 1 WHERE id_viaje = :viajeId AND plazas_disponibles < plazas_totales")
    suspend fun incrementPlazas(viajeId: Long)

    @Query("UPDATE viajes SET estado = 'cancelado', descripcion_cancelacion = :descripcion WHERE id_viaje = :viajeId")
    suspend fun cancelarViaje(viajeId: Long, descripcion: String)

    @Query("SELECT * FROM viajes WHERE id_viaje = :viajeId")
    suspend fun getViajeById(viajeId: Long): Viaje?

    @Query("SELECT * FROM viajes WHERE id_conductor = :conductorId ORDER BY fecha_salida, hora_salida")
    fun getViajesByConductorAsFlow(conductorId: Long): Flow<List<Viaje>>

    @Query("DELETE FROM viajes WHERE id_viaje = :viajeId")
    suspend fun deleteViaje(viajeId: Long)

    // En ViajeDao.kt, añade:
    @Query("SELECT * FROM viajes WHERE id_conductor = :conductorId AND estado = 'activo' ORDER BY fecha_salida, hora_salida")
    suspend fun getViajesByConductor(conductorId: Long): List<Viaje>

    @Query("UPDATE viajes SET nombre_conductor = :newName WHERE id_conductor = :userId")
    suspend fun updateConductorNameInViajes(userId: Long, newName: String)

    // En ViajeDao.kt

    @Query("""
    SELECT * FROM viajes 
    WHERE estado = 'activo' 
    AND (origen LIKE '%' || :origen || '%' OR :origen = '')
    AND (destino LIKE '%' || :destino || '%' OR :destino = '')
    AND (fecha_salida LIKE :fecha || '%' OR :fecha = '')
    AND (precio <= :precioMax OR :precioMax = 0)
    AND (plazas_disponibles >= :plazasMin OR :plazasMin = 0)
    ORDER BY fecha_salida, hora_salida
""")
    fun searchViajesFiltrados(
        origen: String,
        destino: String,
        fecha: String,
        precioMax: Double,
        plazasMin: Int
    ): Flow<List<Viaje>>

    @Query("""
    SELECT COUNT(*) > 0 FROM viajes 
    WHERE id_conductor = :conductorId 
    AND estado = 'activo'
    AND fecha_salida = :fechaSalida
    AND (
        (hora_salida <= :horaLlegada AND hora_llegada >= :horaSalida)
    )
""")
    suspend fun hasOverlappingRide(
        conductorId: Long,
        fechaSalida: String,
        horaSalida: String,
        horaLlegada: String
    ): Boolean

    // En ViajeDao.kt

    @Query("""
    UPDATE viajes SET estado = 'completado'
    WHERE estado = 'activo' AND (
        fecha_llegada < :fechaActual OR 
        (fecha_llegada = :fechaActual AND hora_llegada <= :horaActual)
    )
""")
    suspend fun updateCompletedViajes(fechaActual: String, horaActual: String)
}
