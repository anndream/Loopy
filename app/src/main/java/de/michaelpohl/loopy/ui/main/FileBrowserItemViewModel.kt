package de.michaelpohl.loopy.ui.main

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.view.View.*
import de.michaelpohl.loopy.common.FileHelper
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.FileType
import hugo.weaving.DebugLog

@DebugLog
class FileBrowserItemViewModel : ViewModel() {

    val folderLabelVisibility = ObservableField(INVISIBLE)
    val sizeLabelVisibility = ObservableField(INVISIBLE)
    val subFolderIndicatorVisibility = ObservableField(INVISIBLE)
    val pickFolderButtonVisibility = ObservableField(INVISIBLE)

    var fileModel = FileModel("", FileType.FILE, "", 0.0)
    var name = ObservableField("name")
    var subFolders = ObservableField("folders")
    var fileSize = ObservableField("filesize")

    fun update() {
        name.set(fileModel.name)
        //TODO turn this string stuff into something proper and non-hard coded
        subFolders.set("(${fileModel.subFiles} files)")
        fileSize.set("${String.format("%.2f", fileModel.sizeInMB)} mb")


        if (fileModel.fileType == FileType.FOLDER) {
            folderLabelVisibility.set(VISIBLE)
            sizeLabelVisibility.set(INVISIBLE)
        } else {

            folderLabelVisibility.set(INVISIBLE)
            sizeLabelVisibility.set(VISIBLE)
        }
        if (fileModel.hasSubFolders()) {

            subFolderIndicatorVisibility.set(VISIBLE)
        } else {

            subFolderIndicatorVisibility.set(INVISIBLE)
        }
        if (FileHelper.containsAudioFilesInAnySubFolders(fileModel.path)) {

            pickFolderButtonVisibility.set(VISIBLE)
        } else {

            pickFolderButtonVisibility.set(GONE)
        }
    }
}