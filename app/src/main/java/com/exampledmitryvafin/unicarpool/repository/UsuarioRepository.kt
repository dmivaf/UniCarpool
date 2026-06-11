package com.exampledmitryvafin.unicarpool.repository

import com.exampledmitryvafin.unicarpool.data.dao.UsuarioDao
import com.exampledmitryvafin.unicarpool.data.entity.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Registrar un nuevo usuario
    suspend fun registerUser(usuario: Usuario): Long {
        return usuarioDao.insert(usuario)
    }

    // Iniciar sesión (buscar por email y contraseña)
    suspend fun login(correo: String, contrasena: String): Usuario? {
        return usuarioDao.login(correo, contrasena)
    }

    // Buscar usuario por email (para validar que no exista)
    suspend fun getUserByEmail(correo: String): Usuario? {
        return usuarioDao.getUsuarioByCorreo(correo)
    }

    // Actualizar nombre del usuario
    suspend fun updateUserName(userId: Long, newName: String) {
        usuarioDao.updateNombre(userId, newName)
    }

    // Eliminar usuario
    suspend fun deleteUser(userId: Long) {
        usuarioDao.deleteById(userId)
    }

    // En UsuarioRepository.kt, añade esta función:
    suspend fun getUserById(userId: Long): Usuario? {
        return usuarioDao.getUserById(userId)
    }

    suspend fun updatePassword(userId: Long, newPassword: String) {
        usuarioDao.updatePassword(userId, newPassword)
    }
}