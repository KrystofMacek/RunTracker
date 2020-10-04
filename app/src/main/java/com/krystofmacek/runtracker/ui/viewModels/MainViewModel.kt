package com.krystofmacek.runtracker.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krystofmacek.runtracker.database.Run
import com.krystofmacek.runtracker.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val repository: MainRepository
): ViewModel() {

    val runsSortedByDate = repository.getAllRunsSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        repository.insertRun(run)
    }

}