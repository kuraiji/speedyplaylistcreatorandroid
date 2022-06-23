package com.kuraiji.speedyplaylistcreator.domain

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LandingViewModel : ViewModel() {
    val notEntered = mutableStateOf(true)
    val started = mutableStateOf(false)
}