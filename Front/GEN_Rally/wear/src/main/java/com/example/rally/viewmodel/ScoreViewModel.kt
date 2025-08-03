package com.example.rally.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScoreViewModel : ViewModel() {

    private val _userScore = MutableStateFlow(0)
    val userScore: StateFlow<Int> = _userScore

    private val _userSets = MutableStateFlow(0)
    val userSets: StateFlow<Int> = _userSets

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore

    private val _opponentSets = MutableStateFlow(0)
    val opponentSets: StateFlow<Int> = _opponentSets

    private val _userServe = MutableStateFlow(true)
    val userServe: StateFlow<Boolean> = _userServe

    fun addUserScore() {
        _userScore.value += 1
    }

    fun undoUserScore() {
        if (_userScore.value > 0) _userScore.value -= 1
    }

    fun toggleServe() {
        _userServe.value = !_userServe.value
    }
}