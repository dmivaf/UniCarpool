package com.exampledmitryvafin.unicarpool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository

class ViajeViewModelFactory(
    private val viajeRepository: ViajeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViajeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViajeViewModel(viajeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}