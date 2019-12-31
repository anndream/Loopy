package de.michaelpohl.loopy.model

import android.content.res.AssetManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class AudioFilesRepository(
    private val sharedPrefsManager: SharedPreferencesManager,
    private val storage: ExternalStorageManager
) : KoinComponent {

    private val assets: AssetManager by inject()

    fun getSavedSets(): Sets {
        return sharedPrefsManager.loadLoopSets()
    }

    fun saveSet(set: LoopSet) {
        sharedPrefsManager.saveSingleSet(set)
    }

    fun getSingleSet(): LoopSet {
        //TODO fill with real functionality
        return LoopSet("", listOf())
    }

    fun autoCreateStandardLoopSet() {
        val wasFolderCreated =  storage.createSetFolder()
        Timber.d("Was the folder created? $wasFolderCreated")
        // TODO remove hardcoded thing here!
        val fileName = "testing.mp3"
        storage.listAssetFiles()
        storage.copyStandardFilesToSdCard(assets.open(fileName), fileName)
    }


}