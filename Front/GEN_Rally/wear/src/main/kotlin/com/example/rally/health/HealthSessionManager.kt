import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Parcelable
import android.util.Log
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.StatisticalDataPoint
import androidx.health.services.client.endExercise
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.IntervalDataPoint
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import kotlinx.parcelize.Parcelize
import kotlin.math.min

// 세트별 헬스 데이터 요약
@Parcelize
data class SetHealthSummary(
    val maxHeartRateBpm: Int?,
    val minHeartRateBpm: Int?,
    val heartSeries: List<HeartSample>,
    val steps: Long,
    val caloriesKcal: Int,
) :Parcelable

@Parcelize
data class HeartSample(val epochMs: Long, val bpm: Int) : Parcelable

// 최종 헬스 데이터
@Parcelize
data class GameHealthPayload (
    val perSet: List<SetHealthSummary>,
    val totalSteps: Long,
    val totalKcal: Int,
    val overallMaxBpm: Int?,
    val overallMinBpm: Int?
) : Parcelable {
    companion object {
        fun from(sets: List<SetHealthSummary>): GameHealthPayload {
            val totalSteps = sets.sumOf { it.steps }
            val totalKcal  = sets.sumOf { it.caloriesKcal }
            val overallMax = sets.mapNotNull { it.maxHeartRateBpm }.maxOrNull()
            val overallMin = sets.mapNotNull { it.minHeartRateBpm }.minOrNull()

            return GameHealthPayload(
                perSet = sets,
                totalSteps = totalSteps,
                totalKcal = totalKcal,
                overallMaxBpm = overallMax,
                overallMinBpm = overallMin
            )
        }
    }
}

class HealthSessionManager(private val context: Context) {

    private val exerciseClient: ExerciseClient =
        HealthServices.getClient(context).exerciseClient

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var segBaselineCounter: Long? = null
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(ev: SensorEvent) {
            val v = ev.values.firstOrNull()?.toLong() ?: return
            lastSteps = v

            if (isActive && segBaselineCounter == null) {
                segBaselineCounter = v
                Log.d("HS/STEPS", "baseline set: $v")
            }
        }
        override fun onAccuracyChanged(sensor:  Sensor?, accuracy: Int) {}
    }
    private var isStepListenerRegistered = false

    private var totalSteps: Long = 0L
    private var totalKcal: Double = 0.0
    private val heartSeries = mutableListOf<HeartSample>()
    private var hrMax: Int? = null
    private var hrMin: Int? = null

    private var segmentStartKcal: Double? = null

    private var lastSteps: Long = 0L
    private var lastKcal: Double = 0.0

    private var isActive = false
    private var isCallbackRegistered = false
    private var updateCallback: ExerciseUpdateCallback? = null

    suspend fun ownsActiveExercise(): Boolean {
        val info = exerciseClient.getCurrentExerciseInfo()
        return info.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun beginSet() {
        resetAll()

        val config = ExerciseConfig.Builder(ExerciseType.BADMINTON)
            .setDataTypes(setOf(DataType.HEART_RATE_BPM, DataType.CALORIES))
            .build()

        registerUpdateCallbackIfNeeded()
        exerciseClient.startExercise(config)

        startStepSensorIfAvailable()
        markSegmentStart()
        isActive = true
    }

    suspend fun pauseSet() {
        if (!isActive) return
        settleActiveSegment()
        isActive = false
        exerciseClient.pauseExercise()
        stopStepSensorIfNeeded()
    }

    suspend fun resumeSet() {
        exerciseClient.resumeExercise()
        markSegmentStart()
        isActive = true
        startStepSensorIfAvailable()
    }

    suspend fun endSet(): SetHealthSummary = withContext(Dispatchers.Default) {
        try {
            if (isActive) settleActiveSegment()
            stopStepSensorIfNeeded()

            val owned = runCatching { ownsActiveExercise() }.getOrDefault(false) // 소유 재확인
            clearCallbackIfAny()

            if (owned) {
                exerciseClient.endExercise()
                Log.d("HS", "endExercise OK")
            } else {
                 Log.w("HS", "endExercise skipped: no active exercise")
            }
        } catch (t: Throwable) {
             Log.e("HS", "endSet() internal error", t)
        }

        val summary = SetHealthSummary(
            maxHeartRateBpm = hrMax,
            minHeartRateBpm = hrMin,
            heartSeries = heartSeries.toList(),
            steps = totalSteps,
            caloriesKcal = totalKcal.toInt()
        )
        resetAll()
        summary
    }

    private fun registerUpdateCallbackIfNeeded() {
        if (isCallbackRegistered) return

        try {
            // 콜백 객체 만들기
            val cb = object : ExerciseUpdateCallback {

                override fun onRegistered() {
                    Log.d("HS", "callback registered")
                }

                override fun onRegistrationFailed(throwable: Throwable) {
                    Log.e("HS", "callback registration failed", throwable)
                }

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    try {
                        // 상태 동기화
                        val st = update.exerciseStateInfo?.state
                        if (st?.isPaused == true && isActive) {
                            settleActiveSegment()
                            isActive = false
                        } else if (st != null && !st.isPaused && !st.isEnded && !isActive) {
                            markSegmentStart()
                            isActive = true
                        }
                        val container = update.latestMetrics ?: return

                        // 칼로리
                        container.getData(DataType.CALORIES)?.forEach { dp ->
                            when (dp) {
                                is IntervalDataPoint<*> -> {
                                    if (isActive) {
                                        val delta = (dp.value as Number).toDouble()
                                        lastKcal += delta
                                    }
                                }
                            }
                        }
                        // 심박수
                        var handledHr = false
                        (container.getData(DataType.HEART_RATE_BPM) ?: emptyList()).forEach { dp ->
                            (dp as? StatisticalDataPoint<*>)?.let {
                                if (isActive) {
                                    val mxD = (it.max as Number).toDouble()
                                    val mnD = (it.min as? Number)?.toDouble() ?: Double.NaN

                                    // 유효값만 사용 (NaN/0/음수 거르기)
                                    if (mxD.isFinite() && mxD > 0) {
                                        val mx = mxD.toInt()
                                        val mn = if (mnD.isFinite() && mnD > 0) mnD.toInt() else mx

                                        hrMax = kotlin.math.max(hrMax ?: mx, mx)
                                        hrMin = if (hrMin == null) mn else kotlin.math.min(hrMin!!, mn)
                                        heartSeries += HeartSample(System.currentTimeMillis(), mx)
                                    }
                                }
                                handledHr = true
                            }
                        }
                        // 샘플형 데이터
                        if (!handledHr) {
                            (container.getData(DataType.HEART_RATE_BPM) ?: emptyList()).forEach { dp ->
                                (dp as? SampleDataPoint<*>)?.value?.let { v ->
                                    val bpm = (v as Number).toInt()
                                    if (isActive && bpm in 30..230) {
                                        hrMax = max(hrMax ?: bpm, bpm)
                                        hrMin = if (hrMin == null) bpm else kotlin.math.min(hrMin!!, bpm)
                                        heartSeries += HeartSample(System.currentTimeMillis(), bpm)
                                    }
                                }
                            }
                        }

                    } catch (t: Throwable) {
                        Log.e("HS", "onExerciseUpdateReceived crashed", t)
                    }
                }

                override fun onAvailabilityChanged(
                    dataType: DataType<*, *>, availability: Availability
                ) { }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) { /* optional */ }
            }

            // 메인 실행자 확보 (널 방지)
            val exec = try {
                androidx.core.content.ContextCompat.getMainExecutor(context)
            } catch (t: Throwable) {
                null
            } ?: java.util.concurrent.Executor { r ->
                android.os.Handler(android.os.Looper.getMainLooper()).post(r)
            }

            //  등록
            exerciseClient.setUpdateCallback(exec, cb)
            updateCallback = cb
            isCallbackRegistered = true
            android.util.Log.d("HS", "setUpdateCallback OK")

        } catch (t: Throwable) {
            android.util.Log.e("HS", "registerUpdateCallbackIfNeeded failed", t)
            // 등록 실패 시 상태 롤백
            updateCallback = null
            isCallbackRegistered = false
            throw t
        }
    }
    private fun startStepSensorIfAvailable() {
        if (!isStepListenerRegistered && stepCounterSensor != null) {
            sensorManager.registerListener(
                stepListener, stepCounterSensor,
                android.hardware.SensorManager.SENSOR_DELAY_NORMAL
            )
            isStepListenerRegistered = true
            Log.d("HS", "Step sensor registered")
        }
    }

    private fun stopStepSensorIfNeeded() {
        if (isStepListenerRegistered) {
            sensorManager.unregisterListener(stepListener)
            isStepListenerRegistered = false
            Log.d("HS", "Step sensor unregistered")
        }
    }

    private suspend fun clearCallbackIfAny() {
        try {
            updateCallback?.let { cb ->
                exerciseClient.clearUpdateCallback(cb)
                Log.d("HS", "clearUpdateCallback OK")
            }
        } catch (t: Throwable) {
            Log.e("HS", "clearUpdateCallback failed", t)
        } finally {
            updateCallback = null
            isCallbackRegistered = false
        }
    }

    private fun markSegmentStart() {
        segmentStartKcal = lastKcal
        segBaselineCounter = null
    }

    private fun settleActiveSegment() {
        // 걸음수: 현재누적 - 베이스라인
        segBaselineCounter?.let { base ->
            val delta = (lastSteps - base).coerceAtLeast(0L)
            totalSteps += delta
            Log.d("HS/STEPS", "segment settle: base=$base last=$lastSteps +$delta -> total=$totalSteps")
        }
        segBaselineCounter = null

        // 칼로리: 기존 로직
        segmentStartKcal?.let { start ->
            val d = (lastKcal - start).coerceAtLeast(0.0)
            totalKcal += d
            Log.d("HS/CAL", "segment settle: +$d -> total=$totalKcal")
        }
        segmentStartKcal = null
    }

    private fun resetAll() {
        totalSteps = 0L
        totalKcal = 0.0
        hrMax = null
        hrMin = null
        heartSeries.clear()
        segmentStartKcal = null
        lastSteps = 0L
        lastKcal = 0.0
        isActive = false
    }
}