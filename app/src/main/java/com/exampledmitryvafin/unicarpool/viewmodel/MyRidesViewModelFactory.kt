package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository

class MyRidesViewModelFactory(
    private val viajeRepository: ViajeRepository,
    private val participacionRepository: ParticipacionRepository,
    private val currentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRidesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRidesViewModel(viajeRepository, participacionRepository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}