package de.michaelpohl.loopy.ui.main.filebrowser

import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.michaelpohl.loopy.R
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.immutable
import de.michaelpohl.loopy.ui.main.base.BaseUIState
import de.michaelpohl.loopy.ui.main.base.BaseViewModel

abstract class BrowserViewModel : BaseViewModel<BaseUIState>() {

    protected abstract val selectedFiles: MutableLiveData<List<FileModel.AudioFile>>

    abstract fun submitSelection()

    abstract fun submitAll()

}
