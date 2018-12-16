package de.michaelpohl.loopy.ui.main

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.os.Handler
import android.view.View
import de.michaelpohl.loopy.common.FileHelper
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.model.LoopedPlayer

class PlayerViewModel(application: Application) : BaseViewModel(application) {

    var looper: LoopedPlayer = LoopedPlayer.create(application)
    private var adapter = LoopsAdapter(application)
    lateinit var selectFolderListener: OnSelectFolderClickedListener
    lateinit var loopsList: List<FileModel>
    lateinit var sharedPrefs: SharedPreferences
    var emptyMessageVisibility = ObservableField(View.VISIBLE)

    private var updateHandler = Handler()

    private var updateRunnable = object : Runnable {
        override fun run() {
            adapter.updateProgress(looper.getCurrentPosition())
            updateHandler.postDelayed(this, 40)
        }
    }


    fun getAdapter(): LoopsAdapter {
        return adapter
    }

    fun onStartClicked(view: View) {
        if (looper.hasLoopFile) looper.start()
    }

    fun onStopClicked(view: View) {
        looper.stop()
    }

    fun onPauseClicked(view: View) {
        if (looper.isPlaying()) looper.pause() else if (looper.isPaused) looper.start()
    }

    fun onSelectFolderClicked(view: View) {
        selectFolderListener.onSelectFolderClicked()
    }

    interface OnSelectFolderClickedListener {
        fun onSelectFolderClicked()
    }

    fun updateData() {
        adapter.updateData(loopsList)
        if (adapter.itemCount != 0) {
            emptyMessageVisibility.set(View.INVISIBLE)
        } else {
            emptyMessageVisibility.set(View.VISIBLE)
        }
    }

    fun onItemSelected(fm: FileModel, position: Int) {
        looper.setLoop(getApplication(), FileHelper.getSingleFile(fm.path))
        adapter.selectedPosition = position
        adapter.updateData(adapter.loopsList)
        updateRunnable.run()
        looper.start()
    }
}
