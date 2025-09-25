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
    private val _setNumber = MutableStateFlow(1)
    val setNumber: StateFlow<Int> = _setNumber

    private val _userSets = MutableStateFlow(0)
    val userSets: StateFlow<Int> = _userSets

    private val _userScore = MutableStateFlow(0)
    val userScore: StateFlow<Int> = _userScore

    private val _userName = MutableStateFlow("나")
    val userName: StateFlow<String> = _userName

    private val _opponentSets = MutableStateFlow(0)
    val opponentSets: StateFlow<Int> = _opponentSets

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore

    private val _opponentName = MutableStateFlow("상대")
    val opponentName: StateFlow<String> = _opponentName

    private val _isUser1 = MutableStateFlow(false)   // 로컬 유저가 user1인지 (테스트는 user2로 진행)
    val isUser1: StateFlow<Boolean> = _isUser1

    private val _currentServer = MutableStateFlow(Player.USER1)
    val currentServer: StateFlow<Player> = _currentServer

    private fun localPlayer(): Player =
        if (_isUser1.value) Player.USER1 else Player.USER2
    private fun opponentPlayer(): Player =
        if (_isUser1.value) Player.USER2 else Player.USER1

    fun setNames(user: String, opponent: String) {
        _userName.value = user
        _opponentName.value = opponent
    }
    fun updateSetNumber(num: Int) {
        _setNumber.value = num
    }

    // 직전 상태 복원 위한 내역 저장
    private data class Action (val preServer: Player, val scorer: Player)
    private val history = ArrayDeque<Action>()
    fun noteRally(preServer: Player, scorer: Player) {
        history.addLast(Action(preServer=preServer, scorer=scorer))
        _currentServer.value = scorer
    }
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

    private val _hasStarted = MutableStateFlow(false)
    val hasStarted: StateFlow<Boolean> = _hasStarted
    fun markStarted(started: Boolean) { _hasStarted.value = started }
    fun markGameFinished(finished: Boolean) { _isGameFinished.value = finished}

    // 세트 초기화
    fun initSets(user:Int, opponent: Int) {
        _userSets.value = user
        _opponentSets.value = opponent
    }
    fun initPlayer (isUser1: Boolean) {
        _isUser1.value= isUser1
    }
    // 세트 시작
    fun startSet(setNumber: Int, firstServer: Player) {
        if(_isGameFinished.value) return
        _setNumber.value = setNumber
        _currentServer.value = firstServer
        _userScore.value = 0
        _opponentScore.value = 0
        _isSetFinished.value = false
        history.clear()  // 세트 시작시 내역 초기화
    }

    // 세트 종료
    private val _isSetFinished = mutableStateOf(false)
    private var lastAppliedFinishedSet: Int = 0
    val isSetFinished: State<Boolean> = _isSetFinished
    fun setFinished() {
        _isSetFinished.value = true
    }

    // 게임 종료
    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished

    // 점수 스냅샷 반영
    fun applyScoreSnapshot(my: Int, opp: Int) {
        _userScore.value = my
        _opponentScore.value = opp
    }

    //  상대 점수 증감(백업 플랜)
    fun applyOpponentScoreDelta(delta: Int) {
        val cur = _opponentScore.value
        _opponentScore.value = (cur + delta).coerceAtLeast(0)
    }

    // 세트 종료 처리
    // 경기 결과 계산만 수행, 상태 변경 X
    fun computeSetResult(): SetResult {
        val user = _userScore.value
        val opp  = _opponentScore.value
        val winnerIsUser = user > opp
        val userSetsFuture     = _userSets.value + if (winnerIsUser) 1 else 0
        val opponentSetsFuture = _opponentSets.value + if (winnerIsUser) 0 else 1
        val nextSet = userSetsFuture + opponentSetsFuture + 1
        val nextFirst = if (winnerIsUser) (if (_isUser1.value) Player.USER1 else Player.USER2)
        else (if (_isUser1.value) Player.USER2 else Player.USER1)
        val gameFinished = (userSetsFuture >= 2 || opponentSetsFuture >= 2)

        return SetResult(
            nextSetNumber = nextSet,
            userScore = user,
            userSets = userSetsFuture,
            opponentSets = opponentSetsFuture,
            opponentScore = opp,
            currentServer = nextFirst,
            isGameFinished = gameFinished
        )
    }

    // set_finish 이벤트 수신 시 상태 변경
    fun applySetFinishFromRemote(
        setNumberFinished: Int,
        user1Score: Int,
        user2Score: Int,
        isGameFinishedRemote: Boolean,
        nextFirstServer: Player? = null,
        user1Sets: Int? = null,
        user2Sets: Int? = null
    ) {
        if (setNumberFinished <= lastAppliedFinishedSet) return
        lastAppliedFinishedSet = setNumberFinished

        val iAmUser1 = _isUser1.value
        val hasAbsoluteSets =
            (user1Sets != null && user1Sets >= 0) &&
                    (user2Sets != null && user2Sets >= 0)

        if (hasAbsoluteSets) {
            _userSets.value     = if (iAmUser1) user1Sets!! else user2Sets!!
            _opponentSets.value = if (iAmUser1) user2Sets!! else user1Sets!!
        } else {
            var winner: Player? = when {
                user1Score >= 0 && user2Score >= 0 && user1Score != user2Score ->
                    if (user1Score > user2Score) Player.USER1 else Player.USER2
                else -> null
            }
            if (winner == null && nextFirstServer != null) winner = nextFirstServer

            winner?.let {
                if (it == Player.USER1) {
                    if (iAmUser1) _userSets.value += 1 else _opponentSets.value += 1
                } else {
                    if (iAmUser1) _opponentSets.value += 1 else _userSets.value += 1
                }
            }
        }
        _currentServer.value = nextFirstServer ?: _currentServer.value
        // 세트 종료 처리
        if(!isGameFinishedRemote) {
            _userScore.value = 0
            _opponentScore.value = 0
        } else {
            _isGameFinished.value = true
        }
        _isSetFinished.value = true
        _setNumber.value = setNumberFinished
        resetStopwatch()
    }

    fun applySetFinishLocal(
        finishedSetNumber: Int,
        winner: Player,
        nextFirstServer: Player? = null,
        gameFinished: Boolean
    ) {
        // 이미 처리한 세트면 무시
        if (finishedSetNumber <= lastAppliedFinishedSet) return
        lastAppliedFinishedSet = finishedSetNumber

        val iAmUser1 = _isUser1.value
        // sets 증가
        if (winner == Player.USER1) {
            if (iAmUser1) _userSets.value += 1 else _opponentSets.value += 1
        } else {
            if (iAmUser1) _opponentSets.value += 1 else _userSets.value += 1
        }

        // 다음 세트 준비
        _currentServer.value = nextFirstServer ?: winner
        if(!gameFinished) {
            _userScore.value = 0
            _opponentScore.value = 0
        } else {
            _isGameFinished.value = true
        }
        _isSetFinished.value = true
        resetStopwatch()
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
    fun startStopwatchAt(epochMillisUtc: Long) {
        startTime = epochMillisUtc
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
    fun pauseAt(epochMillisUtc: Long) {
        if (!_isPaused.value) {
            _isPaused.value = true
            pauseStartedAt = epochMillisUtc
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
    fun resumeAt(epochMillisUtc: Long) {
        if (_isPaused.value) {
            pauseStartedAt?.let { totalPaused += (epochMillisUtc - it) }
            pauseStartedAt = null
            _isPaused.value = false
        }
    }

    fun resetStopwatch() {
        timerJob?.cancel()
        startTime = 0L
        totalPaused = 0L
        pauseStartedAt = null
        _elapsed.value = 0L
        _isPaused.value = true
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