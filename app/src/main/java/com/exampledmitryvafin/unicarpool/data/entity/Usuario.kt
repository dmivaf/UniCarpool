package com.exampledmitryvafin.unicarpool.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id_usuario: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val fecha_registro: String,
    val preguntaSeguridad: String = "",   // NUEVO
    val respuestaSeguridad: String = ""   // NUEVO (en producción habría que hashear)
)

