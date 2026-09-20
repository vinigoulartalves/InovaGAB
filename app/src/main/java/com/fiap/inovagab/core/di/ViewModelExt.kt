package com.fiap.inovagab.core.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.InovaGabApp

@Composable
inline fun <reified VM : ViewModel> inovaViewModel(): VM {
    val factory = InovaGabApp.instance.viewModelFactory
    return viewModel(factory = factory)
}
