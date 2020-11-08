package de.michaelpohl.loopy.model

import android.content.res.AssetManager
import de.michaelpohl.loopy.common.AudioModel
import de.michaelpohl.loopy.common.FileModel
import de.michaelpohl.loopy.common.jni.JniBridge
import org.koin.core.KoinComponent
import org.koin.core.inject

class AudioFilesRepository(
    private val sharedPrefsManager: SharedPreferencesManager,
    private val storage: ExternalStorageManager
) : KoinComponent {

    private val assets: AssetManager by inject()

    fun saveSet(set: LoopSet) {
        sharedPrefsManager.saveSingleSet(set)
    }

    /**
     * Load a) the set from the specified folder, b) the saved last selected loops, c) the standard set
     */
    fun getSingleSetOrStandardSet(setFolderName: String? = null): List<AudioModel> {
        return if (setFolderName != null) {
            storage.getAudioModelsInSet(setFolderName)
        } else {
            // get what was open last time
//            sharedPrefsManager.loadLastLoopSelection()?.toAudioModels() ?:

            // get the basic standard set
            storage.getAudioModelsInSet(STANDARD_SET_FOLDER_NAME)
        }
    }

    suspend fun convertFilesInSet(setFolderName: String = STANDARD_SET_FOLDER_NAME): Boolean {
        return JniBridge.convertFilesInFolder(storage.getFullPath(setFolderName))
    }

    // TODO move the whole standard loop set stuff into a separate class.
    //  It clutters this one, and that's not necessary for "execute once ever" code.

    /**
     * Creates the standard set folder and moves all found audio files from assets to there
     * @return true if everything was successful
     */
    fun autoCreateStandardLoopSet(): Boolean {
        return if (storage.createSetFolder() && storage.copyStandardFilesToSdCard()) {
            sharedPrefsManager.selectedSetName = STANDARD_SET_FOLDER_NAME
            true
        } else false
    }

    fun addLoopsToSet(
        newLoops: List<FileModel.AudioFile>,
        setName: String? = null
    ) {

        JniBridge.convertAndAddToSet(
            newLoops, storage.getFullPath(setName ?: STANDARD_SET_FOLDER_NAME)
        )


    }

    fun saveLoopSelection(loopsList: MutableList<AudioModel>) {
        sharedPrefsManager.saveLoopSelection(loopsList)
    }
}
