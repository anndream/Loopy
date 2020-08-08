package de.michaelpohl.loopy.ui.main.filebrowser

import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import de.michaelpohl.loopy.common.StorageRepository
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.FileType
import org.koin.core.KoinComponent
import org.koin.core.inject

class FileBrowserItemViewModel(
    private val position: Int,
    private val fileModel: FileModel,
    private val selectedListener: ((Boolean, Int) -> Unit),
    private val onItemClickedListener: ((FileModel) -> Unit)
) : ViewModel(), KoinComponent {

    private val repo : StorageRepository by inject()

    val folderLabelVisibility = ObservableField(INVISIBLE)
    val sizeLabelVisibility = ObservableField(INVISIBLE)
    val subFolderIndicatorVisibility = ObservableField(INVISIBLE)
    val forbiddenSignVisibility = ObservableField(INVISIBLE)
    val checkBoxVisibility = ObservableField(INVISIBLE)
    val selected = ObservableField(false)

    var name = ObservableField("name")
    var subFolders = ObservableField("folders")
    var fileSize = ObservableField("filesize")

    fun update() {

        //TODO turn this whole method into something more beautiful
        name.set(fileModel.name)
        subFolders.set("(${fileModel.subFiles} files)")
        fileSize.set("${String.format("%.2f", fileModel.sizeInMB)} mb")


        if (fileModel.fileType == FileType.FOLDER) {

            folderLabelVisibility.set(VISIBLE)
            sizeLabelVisibility.set(INVISIBLE)
        } else {

            folderLabelVisibility.set(INVISIBLE)
            checkBoxVisibility.set(VISIBLE)
            sizeLabelVisibility.set(VISIBLE)
        }
        if (fileModel.hasSubFolders()) {

            if (StorageRepository.isExcludedFolderName(fileModel.path)) {
                subFolderIndicatorVisibility.set(INVISIBLE)
                forbiddenSignVisibility.set(VISIBLE)
            } else {
                subFolderIndicatorVisibility.set(VISIBLE)
                forbiddenSignVisibility.set(INVISIBLE)
            }
        }
        selectedListener.invoke(selected.get()!!, position)
    }

    fun onItemClicked(view: View) {
        if (fileModel.fileType == FileType.FILE) {
            onCheckBoxClicked(view)
        } else if (!StorageRepository.isExcludedFolderName(fileModel.path)) {
            onItemClickedListener.invoke(fileModel)
        }
    }

    fun onCheckBoxClicked(view: View) {
        selected.set(selected.get()?.not()) //why this ugly non-null assertion?
        selectedListener.invoke(selected.get()!!, position)
    }

    fun isFolder(): Boolean {
        return fileModel.fileType == FileType.FOLDER
    }
}
