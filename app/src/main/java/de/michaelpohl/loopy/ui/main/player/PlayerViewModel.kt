package de.michaelpohl.loopy.ui.main.player

import android.view.View
import androidx.lifecycle.MediatorLiveData
import de.michaelpohl.loopy.common.AudioModel
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.PlayerState.*
import de.michaelpohl.loopy.common.Settings
import de.michaelpohl.loopy.common.toVisibility
import de.michaelpohl.loopy.common.util.coroutines.uiJob
import de.michaelpohl.loopy.model.AppStateRepository
import de.michaelpohl.loopy.model.AudioFilesRepository
import de.michaelpohl.loopy.model.PlayerServiceInterface
import de.michaelpohl.loopy.ui.main.base.BaseUIState
import de.michaelpohl.loopy.ui.main.base.BaseViewModel
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.system.measureTimeMillis

class PlayerViewModel(
    private val audioFilesRepository: AudioFilesRepository,
    private val appStateRepo: AppStateRepository
) :
    BaseViewModel<PlayerViewModel.UIState>() {

    val isPlaying = MediatorLiveData<Boolean>().apply {
        addSource(_state) {
            this.value = it.isPlaying
        }
    }

    private lateinit var settings: Settings
    private lateinit var looper: PlayerServiceInterface
    lateinit var playerActionsListener: PlayerActionsListener

    override fun initUIState(): UIState {
        return UIState(
            loopsList = audioFilesRepository.getSingleSetOrStandardSet()
                .toMutableList(), // TODO should not be mutable
            isPlaying = false,
            clearButtonVisibility = 0,
            settings = settings,
            processingOverlayVisibility = false.toVisibility()
        )
    }

    override fun onFragmentResumed() {
        settings = appStateRepo.settings
        _state.value = initUIState() // TODO check if this works right
    }


    override fun onFragmentPaused() {
        super.onFragmentPaused()
        uiJob {
            if (!currentState.settings.playInBackground) looper.pause()
        }
    }

    fun setPlayerWaitMode(shouldWait: Boolean = appStateRepo.settings.isWaitMode) {
        if (!::looper.isInitialized || looper.getWaitMode() == shouldWait) return
        uiJob {
            if (looper.setWaitMode(shouldWait).isSuccess()) {
                Timber.v("Looper waitmode set to $shouldWait")
            } else {
                error("Failed to set wait mode. This is a program error.")
            }
            if (!looper.getWaitMode()) _state.value = currentState.copy(filePreselected = "")
        }
    }

    fun setPlayer(player: PlayerServiceInterface) {
        looper = player.apply {
            setFileStartedByPlayerListener { onPlayerSwitchedToNextFile(it) }
            setPlaybackProgressListener { name, value ->
                _state.postValue(currentState.copy(playbackProgress = Pair(name, value)))
            }
        }
    }

    fun onStartPlaybackClicked() {
        uiJob {
            when (looper.getState()) {
                PLAYING -> { /* do nothing */
                }
                PAUSED -> {
                    looper.resume()
                    _state.value = (currentState.copy(isPlaying = true))
                }
                else -> if (looper.hasLoopFile()) startLooper()
            }
        }
    }

    fun onStopPlaybackClicked() {
        when (looper.getState()) {
            PLAYING, PAUSED -> stopLooper()
            else -> { /* do nothing */
            }
        }
    }

    fun onPausePlaybackClicked() {
        uiJob {
            Timber.d("State: ${looper.getState()}")
            when (looper.getState()) {
                PLAYING -> {
                    looper.pause()
                    _state.value = (currentState.copy(isPlaying = false))
                }
                PAUSED -> {
                    looper.resume()
                    _state.value = (currentState.copy(isPlaying = true))

                }
                else -> { /* do nothing */
                }
            }

        }
    }

    // TODO make nicer
    fun onLoopClicked(audioModel: AudioModel) {
        uiJob {
            with(looper.select(audioModel.path)) {
                if (this.isSuccess()) {
                    this.data?.let {
                        onFileSelected(it)
                    } ?: error("Got no filename back from JNI. This shouldn't happen")
                }
            }
        }
    }

    fun stopLooper() {
        uiJob {
            if (looper.stop().isSuccess()) {
                _state.value = (
                        currentState.copy(
                            playbackProgress = Pair(currentState.fileInFocus ?: "", 0),
                            fileInFocus = "",
                            filePreselected = "",
                            isPlaying = false
                        )
                        )
            }
        }
    }

    fun addNewLoops(newLoops: List<FileModel.AudioFile>) {
        // TODO ask the user if adding or replacing is desired
        // TODO this can be extremely slow
        Timber.d("Start conversion") // here loading state
        _state.postValue(currentState.copy(processingOverlayVisibility = true.toVisibility()))
        uiJob {
            val elapsed = measureTimeMillis {
                val result = audioFilesRepository.addLoopsToSet(newLoops)
                Timber.d("Result: $result")
                val loops = audioFilesRepository.getSingleSetOrStandardSet()
                _state.postValue(currentState.copy(loopsList = loops.toMutableList(), processingOverlayVisibility = false.toVisibility()))
            }
            Timber.d("elapsed: $elapsed")
        }

    }

    fun onDeleteLoopClicked(audioModel: AudioModel) {
        val currentLoops = currentState.loopsList.toMutableList()

        currentLoops.remove(audioModel)
        audioFilesRepository.saveLoopSelectionToSet(
            null,
            currentLoops
        ) // TODO set name needs to be properly connected
        _state.value = currentState.copy(
            loopsList = audioFilesRepository.getSingleSetOrStandardSet().toMutableList()
        )
    }

    fun onProgressChangedByUser(newProgress: Float) {
        looper.changePlaybackPosition(newProgress)
    }

    private fun onPlayerSwitchedToNextFile(filename: String) {
        _state.postValue(currentState.copy(fileInFocus = filename))
    }

    private fun onFileSelected(filename: String) {
        if (looper.getWaitMode()) {
            when (looper.getState()) {
                PLAYING, PAUSED, READY -> _state.postValue(currentState.copy(filePreselected = filename))
                STOPPED, UNKNOWN -> startLooper()
            }
        } else {
            startLooper()
        }
    }

    private fun startLooper() {
        uiJob {
            with(looper.play()) {
                if (this.isSuccess()) {
                    this.data?.let {
                        _state.postValue(
                            currentState.copy(
                                fileInFocus = this.data,
                                isPlaying = true
                            )
                        )
                    }
                }
            }
        }
    }

    @Deprecated("I assume?")
    private fun onPlaybackStopped() {
        _state.value = currentState.copy(isPlaying = false)
    }

    data class UIState(
        val loopsList: MutableList<AudioModel>,
        val isPlaying: Boolean,
        val isWaitMode: Boolean = false,
        val fileInFocus: String? = null,
        val filePreselected: String? = null,
        val playbackProgress: Pair<String, Int>? = null,
        val clearButtonVisibility: Int = View.GONE,
        val settings: Settings,
        val processingOverlayVisibility: Int
    ) : BaseUIState() {
        val emptyMessageVisibility: Int = this.loopsList.isEmpty().toVisibility()
    }

    interface PlayerActionsListener {
        fun onOpenFileBrowserClicked()
        fun onBrowseMediaStoreClicked()
    }
}
