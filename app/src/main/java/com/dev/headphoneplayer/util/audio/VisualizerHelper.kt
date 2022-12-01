package com.dev.headphoneplayer.util.audio

import android.media.audiofx.Visualizer

class VisualizerHelper {

    companion object {

        val CAPTURE_SIZE = Visualizer.getCaptureSizeRange()[1]

        const val SAMPLING_INTERVAL = 100
    }

    private var visualizer: Visualizer? = null


    private fun visualizerCallback(onData: (VisualizerData) -> Unit) =
        object : Visualizer.OnDataCaptureListener {

            var lastDataTimestamp: Long? = null

            override fun onWaveFormDataCapture(
                visualizer: Visualizer,
                waveform: ByteArray,
                samplingRate: Int
            ) {
                val now = System.currentTimeMillis()
                val durationSinceLastData = lastDataTimestamp?.let { now - it } ?: 0
                if (lastDataTimestamp == null || durationSinceLastData > SAMPLING_INTERVAL) {
                    onData(
                        VisualizerData(
                            rawWaveform = waveform.clone(),
                            captureSize = CAPTURE_SIZE
                        )
                    )
                    lastDataTimestamp = now
                }
            }

            override fun onFftDataCapture(
                visualizer: Visualizer,
                fft: ByteArray,
                samplingRate: Int
            ) {
            }

        }

    fun start(audioSessionId: Int = 0, onData: (VisualizerData) -> Unit) {
        stop()
        visualizer = Visualizer(audioSessionId).apply {
            enabled = false
            captureSize = CAPTURE_SIZE
            setDataCaptureListener(
                visualizerCallback(onData),
                Visualizer.getMaxCaptureRate(),
                true,
                true
            )
            enabled = true
        }
    }

    fun stop() {
        visualizer?.release()
        visualizer = null
    }
}