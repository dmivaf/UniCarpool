package com.exampledmitryvafin.unicarpool.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viajes")
data class Viaje(
    @PrimaryKey(autoGenerate = true)
    val id_viaje: Long = 0,
    val id_conductor: Long,
    val nombre_conductor: String,
    val origen: String,
    val destino: String,
    val fecha_salida: String,
    val hora_salida: String,
    val fecha_llegada: String,
    val hora_llegada: String,
    val plazas_totales: Int,
    val plazas_disponibles: Int,
    val estado: String,
    val descripcion_cancelacion: String = "",
    val precio: Double = 0.0,
    val created_at: String = ""
)