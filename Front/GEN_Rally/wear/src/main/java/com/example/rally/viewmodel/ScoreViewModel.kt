package com.example.rally.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    fun initSets(user:Int, opponent: Int) {
        _userSets.value = user
        _opponentSets.value = opponent
    }

    fun toggleServe() {
        _userServe.value = !_userServe.value
    }

    // 경기 시간 측정
    private var startTime: Long = 0
    private var totalPaused: Long = 0
    private var pauseStartedAt: Long? = null

    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed

    private var timerJob: Job? = null

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    // 스톱워치 시작
    fun startStopwatch() {
        startTime = System.currentTimeMillis()
        totalPaused = 0
        pauseStartedAt = null
        _isPaused.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_isPaused.value) {
                    val now = System.currentTimeMillis()
                    val elapsedMillis = now - startTime - totalPaused
                    _elapsed.value = elapsedMillis / 1000
                }
            }
        }
    }

    // 일시정지
    fun pause() {
        if (!_isPaused.value) {
            _isPaused.value = true
            pauseStartedAt = System.currentTimeMillis()
        }
    }

    // 재개
    fun resume() {
        if (_isPaused.value) {
            val now = System.currentTimeMillis()
            pauseStartedAt?.let {
                totalPaused += now - it
            }
            pauseStartedAt = null
            _isPaused.value = false
        }
    }

    // 경기 종료 처리
    fun onSetFinished(): SetResult {
        val user = _userScore.value
        val opponent = _opponentScore.value

        val winner = when {
            user > opponent -> {
                _userSets.value += 1
                "user"
            }

            opponent > user -> {
                _opponentSets.value += 1
                "opponent"
            }

            else -> "draw"
        }
        _userScore.value = 0
        _opponentScore.value = 0

        return SetResult(
            //winner = winner,
            nextSetNumber = _userSets.value + _opponentSets.value + 1,
            userSets = _userSets.value,
            opponentSets = _opponentSets.value
        )
    }
}

data class SetResult(
    //val winner: String,
    val nextSetNumber: Int,
    val userSets: Int,            // 사용자가 승리한 세트 수
    val opponentSets: Int         // 상대가 승리한 세트 수
)