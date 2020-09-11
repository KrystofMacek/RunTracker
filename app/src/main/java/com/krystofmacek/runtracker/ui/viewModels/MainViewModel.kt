package com.krystofmacek.runtracker.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.krystofmacek.runtracker.repositories.MainRepository

class MainViewModel @ViewModelInject constructor(
    val repository: MainRepository
): ViewModel() {
}