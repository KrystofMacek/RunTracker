package com.krystofmacek.runtracker.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.krystofmacek.runtracker.repositories.MainRepository


class StatisticsViewModel @ViewModelInject constructor(
    val repository: MainRepository
): ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalDistance = repository.getTotalDistance()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()
    val totalAvgSpeed = repository.getTotalAvgSpeed()

    val runsSortedByDate = repository.getAllRunsSortedByDate()

}