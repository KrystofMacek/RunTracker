package com.krystofmacek.runtracker.ui.fragments


import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.krystofmacek.runtracker.R
import com.krystofmacek.runtracker.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: MainViewModel by viewModels()

}