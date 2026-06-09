package com.exampledmitryvafin.unicarpool.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participaciones")
data class Participacion(
    @PrimaryKey(autoGenerate = true)
    val id_participacion: Long = 0,
    val id_usuario: Long,
    val nombre_pasajero: String,
    val id_viaje: Long,
    val fecha_union: String,
    val estado_participacion: String
)
