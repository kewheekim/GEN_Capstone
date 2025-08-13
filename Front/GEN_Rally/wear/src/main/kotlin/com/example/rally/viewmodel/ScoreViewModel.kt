package com.example.rally.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Player { USER1, USER2 }

class ScoreViewModel : ViewModel() {
    private val _userScore = MutableStateFlow(0)
    val userScore: StateFlow<Int> = _userScore

    private val _userSets = MutableStateFlow(0)
    val userSets: StateFlow<Int> = _userSets

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore

    private val _opponentSets = MutableStateFlow(0)
    val opponentSets: StateFlow<Int> = _opponentSets

    private val _isUser1 = MutableStateFlow(false)   // 로컬 유저가 user1인지 (테스트는 user2로 진행)
    val isUser1: StateFlow<Boolean> = _isUser1

    private val _currentServer = MutableStateFlow(Player.USER1)
    val currentServer: StateFlow<Player> = _currentServer

    private fun localPlayer(): Player =
        if (_isUser1.value) Player.USER1 else Player.USER2
    private fun opponentPlayer(): Player =
        if (_isUser1.value) Player.USER2 else Player.USER1

    // 직전 상태 복원위한 내역 저장
    private data class Action (val preServer: Player, val scorer: Player)
    private val history = ArrayDeque<Action>()
    // 득점
    fun addUserScore() {
        _userScore.value += 1
        history.addLast(Action(preServer = _currentServer.value, scorer = localPlayer()))
        _currentServer.value =localPlayer()
    }
    // 점수 되돌리기
    fun undoUserScore() {
        if (_userScore.value <= 0 || history.isEmpty()) return
        val last = history.removeLast()
        if (last.scorer == localPlayer()) {
            _userScore.value -= 1
            _currentServer.value = last.preServer  // 서브권 직전 상태로 복원
        } else {
            // 마지막 기록이 상대 득점이면 되돌리지 않음
            history.addLast(last)
        }
    }
//    // 상대 점수 반영 (dataLayer)
//    fun applyOpponentScore(newScore: Int) {
//        _opponentScore.value = newScore
//    }

    // 세트 시작
    fun initSets(user:Int, opponent: Int) {
        _userSets.value = user
        _opponentSets.value = opponent
    }
    fun initPlayer (isUser1: Boolean) {
        _isUser1.value= isUser1
    }
    fun startSet(setNumber: Int, firstServer: Player) {
        _currentServer.value = firstServer
        _userScore.value = 0
        _opponentScore.value = 0
        _isSetFinished.value = false
        history.clear()  // 세트 시작시 내역 초기화
    }

    // 세트 종료
    private val _isSetFinished = mutableStateOf(false)
    val isSetFinished: State<Boolean> = _isSetFinished
    fun setFinished() {
        _isSetFinished.value = true
    }

    // 게임 종료
    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished

    // 세트 종료 처리
    fun onSetFinished(): SetResult {
        val user = _userScore.value
        val opponent = _opponentScore.value
        history.clear()   // 세트 종료 시 히스토리 초기화

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

        // 다음 세트 첫 서브
        _currentServer.value = if (winner.equals("user")) {
            if (_isUser1.value) Player.USER1 else Player.USER2
        } else {
            if (_isUser1.value) Player.USER2 else Player.USER1
        }

        // 게임 종료 판정
        if (_userSets.value >= 2 || _opponentSets.value >= 2) {
            _isGameFinished.value = true
        }

        return SetResult(
            nextSetNumber = _userSets.value + _opponentSets.value + 1,
            userSets = _userSets.value,
            userScore=_userScore.value,
            opponentSets = _opponentSets.value,
            opponentScore = _opponentScore.value,
            currentServer = _currentServer.value,
            isGameFinished = _isGameFinished.value
        )
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
}

// 세트 종료 결과 데이터
data class SetResult(
    val nextSetNumber: Int,
    val userScore: Int,
    val userSets: Int,            // 사용자가 승리한 세트 수
    val opponentSets: Int,         // 상대가 승리한 세트 수
    val opponentScore: Int,
    val currentServer: Player,
    val isGameFinished: Boolean
)