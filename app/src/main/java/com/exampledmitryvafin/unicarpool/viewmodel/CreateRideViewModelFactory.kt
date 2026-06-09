package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository

class CreateRideViewModelFactory(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long,
    private val currentUserName: String   // NUEVO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRideViewModel(
                viajeRepository,
                participacionRepository,  // NUEVO
                currentUserId,
                currentUserName
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}