package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository

class RideDetailViewModelFactory(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val currentUserId: Long,
    private val currentUserName: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RideDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RideDetailViewModel(
                viajeRepository,
                participacionRepository,
                usuarioRepository,
                currentUserId,
                currentUserName
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}