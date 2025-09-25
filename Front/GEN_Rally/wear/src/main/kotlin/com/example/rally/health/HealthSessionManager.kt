import android.content.Context
import android.hardware.Sensor
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
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(ev: android.hardware.SensorEvent) {
            // 누적 스텝 (부팅 후 총계)
            lastSteps = ev.values.firstOrNull()?.toLong() ?: lastSteps
        }
        override fun onAccuracyChanged(sensor:  Sensor?, accuracy: Int) {}
    }
    private var isStepListenerRegistered = false

    private var totalSteps: Long = 0L
    private var totalKcal: Double = 0.0

    private val bootInstant by lazy {
        val now = System.currentTimeMillis()
        val sinceBoot = android.os.SystemClock.elapsedRealtime()
        java.time.Instant.ofEpochMilli(now - sinceBoot)
    }
    private val heartSeries = mutableListOf<HeartSample>()
    private var hrSum: Double = 0.0
    private var hrCount: Int = 0
    private var hrMax: Int? = null
    private var hrMin: Int? = null

    private var segmentStartSteps: Long? = null
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

        val caps = exerciseClient.getCapabilities()
        val exCaps = caps.getExerciseTypeCapabilities(ExerciseType.BADMINTON)
        val sup = exCaps.supportedDataTypes

        val wanted = mutableSetOf<DataType<*, *>>()
        wanted += DataType.HEART_RATE_BPM
        wanted += DataType.CALORIES

        val config = ExerciseConfig.Builder(ExerciseType.BADMINTON)
            .setDataTypes(wanted)
            .build()

        registerUpdateCallbackIfNeeded()
        exerciseClient.startExercise(config)
        Log.d("HS", "startExercise OK; requested=$wanted")

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
                        var gotAny = false

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
                                    val avg = (it.average as Number).toInt()
                                    val mx  = (it.max as Number).toInt()
                                    val mn  = runCatching { (it.min as Number).toInt() }.getOrNull() ?: avg

                                    hrSum += avg
                                    hrCount += 1
                                    hrMax =  max(hrMax ?: mx, mx)
                                    hrMin =  min(hrMin ?: mn, mn)

                                    val t = System.currentTimeMillis()
                                    heartSeries += HeartSample(t, avg)
                                }
                            }
                        }
                        // 샘플형 데이터
                        if (!handledHr) {
                            (container.getData(DataType.HEART_RATE_BPM) ?: emptyList()).forEach { dp ->
                                (dp as? SampleDataPoint<*>)?.value?.let { v ->
                                    val bpm = (v as Number).toInt()
                                    if (isActive) {
                                        hrSum += bpm
                                        hrCount += 1
                                        hrMax = kotlin.math.max(hrMax ?: bpm, bpm)
                                        hrMin = kotlin.math.min(hrMin ?: bpm, bpm)

                                        val t = System.currentTimeMillis()
                                        heartSeries += HeartSample(t, bpm)
                                    }
                                }
                            }
                        }
                        if (!gotAny) Log.w("HS", "update with no metrics")

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
        segmentStartSteps = lastSteps
        segmentStartKcal = lastKcal
    }

    private fun settleActiveSegment() {
        segmentStartSteps?.let { start -> totalSteps += (lastSteps - start).coerceAtLeast(0L) }
        segmentStartKcal?.let { start -> totalKcal += (lastKcal - start).coerceAtLeast(0.0) }
        segmentStartSteps = null
        segmentStartKcal = null
    }

    private fun resetAll() {
        totalSteps = 0L
        totalKcal = 0.0
        hrSum = 0.0
        hrCount = 0
        hrMax = null
        hrMin = null
        heartSeries.clear()
        segmentStartSteps = null
        segmentStartKcal = null
        lastSteps = 0L
        lastKcal = 0.0
        isActive = false
    }
}