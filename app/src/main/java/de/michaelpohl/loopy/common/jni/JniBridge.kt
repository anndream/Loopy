package de.michaelpohl.loopy.common.jni

import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object JniBridge {

    private var startJob: Continuation<JniResult<String>>? = null

    var waitMode = false // TODO later the waitmode should come from settings
        private set

    var currentlySelectedFile: String? = null

    var progressListener: ((Int) -> Unit)? = null
    var filePreselectedListener: ((String) -> Unit)? = null
    lateinit var fileStartedByPlayerListener: ((String) -> Unit)
    lateinit var playbackProgressListener: (String, Int) -> Unit

    init {
        System.loadLibrary("native-lib")
        Timber.d("Native Lib loaded!")
    }

    suspend fun setWaitMode(shouldWait: Boolean): JniResult<Boolean> = suspendCoroutine { job ->
        if (setWaitModeNative(shouldWait)) {
            waitMode = shouldWait
            Timber.d("Resuming with $waitMode")
            job.resume(successResult(shouldWait))
        } else {
            job.resume(errorResult()) // shouldn't ever happen, let's see
        }
    }

    suspend fun select(filename: String): JniResult<String> = suspendCoroutine { job ->
        with(selectNative(filename)) {
            job.resume(if (this) successResult(filename) else errorResult())
        }
    }

    suspend fun start(): JniResult<String> = suspendCoroutine {
        startJob = it
        startPlaybackNative()
    }

    suspend fun pause(): JniResult<Nothing> = suspendCoroutine { job ->
        with(pausePlaybackNative()) {
            job.resume(this.toJniResult())
        }
    }

    suspend fun resume(): JniResult<Nothing> = suspendCoroutine { job ->
        with(resumePlaybackNative()) {
            job.resume(this.toJniResult())
        }
    }

    suspend fun stop(): JniResult<Nothing> = suspendCoroutine { job ->
        with(stopPlaybackNative()) {
            job.resume(this.toJniResult())
        }
    }

    fun onSelected(filename: String) {
    }

    fun onStarted(filename: String) {
        startJob?.let {
            it.resume(JniResult.Success(filename))
            startJob = null
        } ?: run {
            fileStartedByPlayerListener(filename)
        }
    }

    fun onPaused(filename: String) {
    }

    fun onStopped(filename: String) {
    }

    fun onPlaybackProgressChanged(filename: String, percentage: Int) {
        playbackProgressListener(filename, percentage)
    }

    fun onFileSelected(value: String) {
        Timber.d("name: $value")
        fileStartedByPlayerListener?.invoke(value)
    }

    fun onFilePreselected(value: String) {
        Timber.d("preselected: name: $value")
        fileStartedByPlayerListener?.invoke(value)
        filePreselectedListener?.invoke(value)
    }

    fun convertFilesInFolder(folderName: String): Boolean {
        Timber.d("JniBridge -> converting")
        return convertFolder(folderName)
    }

    private external fun setWaitModeNative(shouldWait: Boolean): Boolean
    private external fun selectNative(filename: String): Boolean // TODO factor out the wait mode
    private external fun startPlaybackNative()
    private external fun stopPlaybackNative() : Boolean
    private external fun pausePlaybackNative(): Boolean
    private external fun resumePlaybackNative(): Boolean
    private external fun convertFolder(folderName: String) : Boolean

}

