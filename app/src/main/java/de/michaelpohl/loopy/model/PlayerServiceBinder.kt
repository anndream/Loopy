package de.michaelpohl.loopy.model

import android.content.Context
import android.os.Binder
import de.michaelpohl.loopy.common.PlayerState
import de.michaelpohl.loopy.common.SwitchingLoopsBehaviour
import de.michaelpohl.loopy.common.jni.JniResult

class PlayerServiceBinder(serviceContext: Context) : Binder(),
    PlayerServiceInterface {

    //    private var looper = LoopedPlayer.create(serviceContext)
    private var looper = JniPlayer()

    override suspend fun pause(): JniResult<Nothing> {
        return looper.pause()
    }

    override suspend fun stop(): JniResult<Nothing> {
        return looper.stop()
    }

    override suspend fun setWaitMode(shouldWait: Boolean): JniResult<Boolean> {
        return looper.setWaitMode(shouldWait)
    }

    override fun changePlaybackPosition(newPosition: Float) =
        looper.changePlaybackPosition(newPosition)

    override fun resetPreSelection() = looper.resetPreSelection()

    override fun isReady(): Boolean {
        return looper.isReady
    }

    override fun isPaused(): Boolean {
        return looper.state == PlayerState.PAUSED
    }

    override fun getState(): PlayerState {
        return looper.state
    }

    override fun getWaitMode(): Boolean {
        return looper.waitMode
    }

    override fun setHasLoopFile(hasFile: Boolean) {
        looper.hasLoopFile = hasFile
    }

    override fun setOnLoopedListener(receiver: (Int) -> Unit) {
        looper.onLoopedListener = receiver
    }

    override fun setOnLoopSwitchedListener(receiver: () -> Unit) {
        looper.onLoopSwitchedListener = receiver
    }

    override fun setSwitchingLoopsBehaviour(behaviour: SwitchingLoopsBehaviour) {
        looper.switchingLoopsBehaviour = behaviour
    }

    override fun getSwitchingLoopsBehaviour(): SwitchingLoopsBehaviour {
        return looper.switchingLoopsBehaviour
    }

    override fun getCurrentPosition() = looper.getCurrentPosition()

    override fun hasLoopFile(): Boolean {
        return looper.hasLoopFile
    }

    override suspend fun preselect(path: String): JniResult<String> {
        return looper.select(path) //TODO this might not be correct
    }

    override suspend fun select(path: String): JniResult<String> {
        return looper.select(path)
    }

    override suspend fun play(): JniResult<String> {
     return looper.start()
    }

    //    // Destroy audio player.
    //    private fun destroyAudioPlayer() {
    //        if (looper.state == PlayerState.PLAYING) {
    //            looper.stop()
    //        }
    //        //TODO properly release looper
    //        //        looper.release()
    //    }
}
