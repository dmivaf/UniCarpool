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
    val fecha_salida: String,      // "2024-01-25"
    val hora_salida: String,       // "15:30"
    val fecha_llegada: String,     // "2024-01-25"
    val hora_llegada: String,      // "16:45"
    val plazas_totales: Int,
    val plazas_disponibles: Int,
    val estado: String,             // "activo", "completado", "cancelado"
    val descripcion_cancelacion: String = "",
    val precio: Double = 0.0,      // AÑADE ESTE CAMPO SI NO LO TIENES
    val created_at: String = ""
)