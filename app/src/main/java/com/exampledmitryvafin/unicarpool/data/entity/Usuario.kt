package com.exampledmitryvafin.unicarpool.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id_usuario: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,  // En versión futura se encriptará
    val fecha_registro: String
)

