
package com.example.cifrabook

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.ln

class TunerActivity : AppCompatActivity() {

    private lateinit var tunerView: TunerView

    @Volatile
    private var listening = false

    @Volatile
    private var audioRecord: AudioRecord? = null

    private var audioThread: Thread? = null

    private val sampleRate = 44100
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tuner)

        tunerView = findViewById(R.id.tunerView)

        findViewById<ImageButton>(R.id.btnTunerTop).setOnClickListener {
            finish()
        }

        checkMicrophonePermission()
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                Toast.makeText(
                    this,
                    "Permita o acesso ao microfone para afinar o violão.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startListening() {
        if (listening) return

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkMicrophonePermission()
            return
        }

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBuffer <= 0) {
            showMessage("Não foi possível inicializar o áudio.")
            return
        }

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuffer, 16384)
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                showMessage("O microfone não pôde ser inicializado.")
                return
            }

            recorder.startRecording()

            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                recorder.release()
                showMessage("O microfone não começou a gravar.")
                return
            }

            audioRecord = recorder
            listening = true

            audioThread = thread(
                start = true,
                name = "CifrabookTunerAudio"
            ) {
                captureAudio(recorder)
            }

        } catch (_: SecurityException) {
            listening = false
            showMessage("Permissão de microfone não concedida.")
        } catch (_: Exception) {
            listening = false
            showMessage("Erro ao iniciar o microfone.")
        }
    }

    private fun captureAudio(recorder: AudioRecord) {
        val samples = ShortArray(4096)
        var smoothedPitch = 0f

        try {
            while (listening) {
                val count = recorder.read(
                    samples,
                    0,
                    samples.size,
                    AudioRecord.READ_BLOCKING
                )

                if (count <= 0) continue

                val detectedPitch = detectPitch(samples, count)

                val pitch = if (detectedPitch > 0f) {
                    if (smoothedPitch > 0f &&
                        abs(detectedPitch - smoothedPitch) <
                        smoothedPitch * 0.12f
                    ) {
                        smoothedPitch * 0.35f +
                                detectedPitch * 0.65f
                    } else {
                        detectedPitch
                    }.also {
                        smoothedPitch = it
                    }
                } else {
                    smoothedPitch = 0f
                    0f
                }

                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        tunerView.currentPitch = pitch
                    }
                }
            }
        } catch (_: Exception) {
            if (listening) {
                showMessage("A captura de áudio foi interrompida.")
            }
        }
    }

    private fun detectPitch(
        samples: ShortArray,
        count: Int
    ): Float {
        val size = minOf(count, samples.size)
        if (size < 2048) return 0f

        val signal = FloatArray(size)
        var mean = 0.0
        var energy = 0.0

        for (i in 0 until size) {
            val value = samples[i].toDouble()
            mean += value
            energy += value * value
        }

        val rms = sqrt(energy / size) / 32768.0
        if (rms < 0.006) return 0f

        mean /= size

        for (i in 0 until size) {
            signal[i] = (
                    samples[i].toDouble() - mean
                    ).toFloat()
        }

        val minLag = sampleRate / 350
        val maxLag = minOf(sampleRate / 65, size / 2)

        if (maxLag <= minLag) return 0f

        val difference = DoubleArray(maxLag + 1)
        val cmnd = DoubleArray(maxLag + 1)
        cmnd[0] = 1.0

        for (lag in 1..maxLag) {
            var sum = 0.0
            val limit = size - lag

            for (i in 0 until limit) {
                val delta = signal[i].toDouble() -
                        signal[i + lag].toDouble()
                sum += delta * delta
            }

            difference[lag] = sum
        }

        var runningSum = 0.0

        for (lag in 1..maxLag) {
            runningSum += difference[lag]
            cmnd[lag] = if (runningSum > 0.0) {
                difference[lag] * lag / runningSum
            } else {
                1.0
            }
        }

        var bestLag = -1
        val threshold = 0.18
        var lag = minLag

        while (lag < maxLag - 1) {
            if (cmnd[lag] < threshold) {
                while (
                    lag + 1 < maxLag &&
                    cmnd[lag + 1] < cmnd[lag]
                ) {
                    lag++
                }

                bestLag = lag
                break
            }
            lag++
        }

        if (bestLag == -1) {
            var bestValue = Double.MAX_VALUE

            for (candidate in minLag..maxLag) {
                if (cmnd[candidate] < bestValue) {
                    bestValue = cmnd[candidate]
                    bestLag = candidate
                }
            }

            if (bestValue > 0.35) return 0f
        }

        if (bestLag <= 0) return 0f

        var refinedLag = bestLag.toDouble()

        if (bestLag > minLag && bestLag < maxLag) {
            val left = cmnd[bestLag - 1]
            val center = cmnd[bestLag]
            val right = cmnd[bestLag + 1]
            val denominator = left - 2.0 * center + right

            if (abs(denominator) > 1e-10) {
                val correction =
                    0.5 * (left - right) / denominator

                refinedLag += correction.coerceIn(-0.5, 0.5)
            }
        }

        if (refinedLag <= 0.0) return 0f

        val frequency = sampleRate / refinedLag

        return if (frequency in 65.0..350.0) {
            frequency.toFloat()
        } else {
            0f
        }
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun stopListening() {
        listening = false

        val recorder = audioRecord
        audioRecord = null

        try {
            recorder?.stop()
        } catch (_: Exception) {
        }

        try {
            recorder?.release()
        } catch (_: Exception) {
        }

        audioThread = null
    }

    override fun onStop() {
        stopListening()
        super.onStop()
    }
}
