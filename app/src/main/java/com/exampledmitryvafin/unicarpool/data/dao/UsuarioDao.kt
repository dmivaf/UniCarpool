package com.exampledmitryvafin.unicarpool.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.exampledmitryvafin.unicarpool.data.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun login(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun getUsuarioByCorreo(correo: String): Usuario?

    @Insert
    suspend fun insert(usuario: Usuario): Long

    @Query("UPDATE usuarios SET nombre = :nombre WHERE id_usuario = :id")
    suspend fun updateNombre(id: Long, nombre: String)

    @Query("DELETE FROM usuarios WHERE id_usuario = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM usuarios WHERE id_usuario = :userId")
    suspend fun getUserById(userId: Long): Usuario?

    @Query("UPDATE usuarios SET contrasena = :newPassword WHERE id_usuario = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)
}
